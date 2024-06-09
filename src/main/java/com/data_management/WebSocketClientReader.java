package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

/**
 * WebSocket client for receiving and storing data.
 */
public class WebSocketClientReader extends WebSocketClient implements DataReader {
    private DataStorage dataStorage;

    /**
     * Initializes a new WebSocketClientReader.
     *
     * @param serverUri   URI of the WebSocket server
     * @param dataStorage DataStorage instance for saving received data
     */
    public WebSocketClientReader(URI serverUri, DataStorage dataStorage) {
        super(serverUri); // Initialize the WebSocket client with the server URI
        this.dataStorage = dataStorage; // Store reference to DataStorage for later use
    }

    /**
     * Invoked when the WebSocket connection is established.
     *
     * @param handshakedata handshake data from the server
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to the WebSocket server");
    }

    /**
     * Invoked when a message is received from the server.
     *
     * @param message the message received from the server
     */
    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        try {
            // Validate and parse the message
            String[] parts = message.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Message format is invalid");
            }

            // Parse message components
            int patientId = Integer.parseInt(parts[0].trim());
            double measurementValue = Double.parseDouble(parts[1].trim());
            String recordType = parts[2].trim();
            long timestamp = Long.parseLong(parts[3].trim());

            // Add parsed data to DataStorage
            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);

        } catch (NumberFormatException e) {
            // Handle errors in number parsing
            System.err.println("Error parsing numeric data: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // Handle invalid message format errors
            System.err.println("Invalid message received: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle any other unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Invoked when the WebSocket connection is closed.
     *
     * @param code   status code for the closure
     * @param reason reason for the closure
     * @param remote whether the closure was initiated by the remote peer
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " additional info: " + reason);

        // Attempt to reconnect if the connection was closed remotely
        if (remote) {
            System.out.println("Attempting to reconnect...");
            try {
                this.reconnect();
            } catch (Exception e) {
                System.err.println("Reconnection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Invoked when an error occurs.
     *
     * @param ex the exception that occurred
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();

        // Attempt to reconnect in case of an error
        try {
            this.reconnect();
        } catch (Exception e) {
            System.err.println("Reconnection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Connects to the WebSocket server and begins reading data.
     *
     * @param serverUri   URI of the WebSocket server
     * @param dataStorage DataStorage instance for saving received data
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void readData(URI serverUri, DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;

        // Attempt to establish the connection
        try {
            this.connect();
        } catch (Exception e) {
            System.err.println("Connection attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
