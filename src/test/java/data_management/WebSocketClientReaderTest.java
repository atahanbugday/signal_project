package data_management;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for the WebSocketClientReader class.
 */
public class WebSocketClientReaderTest {
    private WebSocketClientReader client;
    private DataStorage mockDataStorage;

    /**
     * Initialize test environment before each test.
     *
     * @throws URISyntaxException if the URI is malformed
     */
    @Before
    public void initialize() throws URISyntaxException {
        mockDataStorage = mock(DataStorage.class);
        client = new WebSocketClientReader(new URI("ws://localhost:8080"), mockDataStorage);
    }

    /**
     * Verifies the onOpen method to ensure the connection message is displayed.
     */
    @Test
    public void verifyOnOpen() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        ServerHandshake handshake = mock(ServerHandshake.class);
        client.onOpen(handshake);

        System.setOut(originalOut);

        assertTrue(outContent.toString().contains("Connected to the WebSocket server"));
    }

    /**
     * Checks the onMessage method with a correctly formatted message.
     */
    @Test
    public void checkOnMessage_withValidMessage() {
        String message = "10, -0.34656395320945643, ECG, 1714748468033";
        client.onMessage(message);
        verify(mockDataStorage).addPatientData(eq(10), eq(-0.34656395320945643), eq("ECG"), eq(1714748468033L));
    }

    /**
     * Ensures onMessage method handles an invalid message format properly.
     */
    @Test
    public void ensureOnMessage_withInvalidFormat() {
        String message = "Invalid message format";
        client.onMessage(message);
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    /**
     * Tests the onMessage method with a message that has a parsing error.
     */
    @Test
    public void testOnMessage_withParsingError() {
        String message = "Patient ID: not_a_number, Timestamp: 1714748468033, Label: ECG, Data: -0.34656395320945643";
        client.onMessage(message);
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    /**
     * Tests the onMessage method when an unexpected error occurs.
     */
    @Test
    public void testOnMessage_withUnexpectedError() {
        WebSocketClientReader spyClient = spy(client);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        doThrow(new RuntimeException("Unexpected error")).when(mockDataStorage).addPatientData(anyInt(), anyDouble(),
                anyString(), anyLong());

        String message = "10, -0.34656395320945643, ECG, 1714748468033";
        spyClient.onMessage(message);

        System.setErr(originalErr);

        assertTrue(errContent.toString().contains("Unexpected error: Unexpected error"));
    }

    /**
     * Verifies the onClose method to ensure the client closes correctly.
     */
    @Test
    public void verifyOnClose() {
        client.onClose(1000, "Normal closure", false);
        assertFalse(client.isOpen());
    }

    /**
     * Tests the onClose method handling an exception during reconnection.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testOnClose_withException() throws IOException {
        WebSocketClientReader spyClient = spy(client);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        doThrow(new RuntimeException("Reconnection failed")).when(spyClient).reconnect();
        spyClient.onClose(1006, "Abnormal closure", true);

        System.setErr(originalErr);

        assertFalse(spyClient.isOpen());
        assertTrue(errContent.toString().contains("Reconnection failed"));
    }

    /**
     * Ensures the onError method attempts reconnection and logs errors.
     *
     * @throws InterruptedException if the current thread is interrupted while
     *                              waiting
     */
    @Test
    public void ensureOnError() throws InterruptedException {
        WebSocketClientReader spyClient = spy(client);
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(spyClient).reconnect();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        Exception testException = new Exception("Test exception");
        spyClient.onError(testException);

        boolean reconnectCalled = latch.await(5, TimeUnit.SECONDS);

        System.setErr(originalErr);

        assertFalse(client.isOpen());
        assertTrue("Reconnect method should be called", reconnectCalled);
        verify(spyClient, atLeastOnce()).reconnect();
        assertTrue(errContent.toString().contains("Test exception"));
    }

    /**
     * Tests the onError method when reconnection attempt fails.
     *
     * @throws InterruptedException if the current thread is interrupted while
     *                              waiting
     */
    @Test
    public void testOnReconnectAttemptFailure() throws InterruptedException {
        WebSocketClientReader spyClient = spy(client);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        doThrow(new RuntimeException("Reconnection failed")).when(spyClient).reconnect();

        Exception testException = new Exception("Test exception");
        spyClient.onError(testException);

        TimeUnit.SECONDS.sleep(1);

        System.setErr(originalErr);

        verify(spyClient, atLeastOnce()).reconnect();
        assertTrue(errContent.toString().contains("Reconnection failed"));
    }

    /**
     * Ensures the reconnect method is invoked during abnormal closure.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void ensureReconnect() throws IOException {
        WebSocketClientReader spyClient = spy(client);
        doNothing().when(spyClient).reconnect();
        spyClient.onClose(1006, "Abnormal closure", true);
        verify(spyClient, atLeastOnce()).reconnect();
    }

    /**
     * Tests the readData method to verify successful connection attempt.
     *
     * @throws URISyntaxException if the URI is malformed
     * @throws IOException        if an I/O error occurs
     */
    @Test
    public void testReadData_withSuccessfulConnection() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(client);

        doNothing().when(spyClient).connect();

        spyClient.readData(new URI("ws://localhost:8080"), mockDataStorage);

        verify(spyClient, times(1)).connect();
    }

    /**
     * Tests the readData method handling a connection attempt failure.
     *
     * @throws URISyntaxException if the URI is malformed
     * @throws IOException        if an I/O error occurs
     */
    @Test
    public void testReadData_withConnectionAttemptFailure() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(client);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        doThrow(new RuntimeException("Connection attempt failed")).when(spyClient).connect();

        spyClient.readData(new URI("ws://localhost:8080"), mockDataStorage);

        System.setErr(originalErr);

        assertTrue(errContent.toString().contains("Connection attempt failed: Connection attempt failed"));
    }
}
