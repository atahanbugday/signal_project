package data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileDataReaderTest {
    private DataStorage dataStorageMock;
    private FileDataReader dataReader;
    private Path tempDirectory;

    @BeforeEach
    void init() throws IOException {
        dataStorageMock = mock(DataStorage.class);
        tempDirectory = Files.createTempDirectory("tempDir");
        dataReader = new FileDataReader(tempDirectory.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(tempDirectory)
                .map(Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                });
    }

    @Test
    void readDataWithValidDirectory() throws IOException {
        Path tempFile = Files.createFile(tempDirectory.resolve("data.txt"));
        Files.write(tempFile, List.of("1,100.0,HeartRate,1714376788030", "2,120.0,BloodPressure,1714376788031"));

        dataReader.readData(dataStorageMock);

        verify(dataStorageMock, times(1)).addPatientData(1, 100.0, "HeartRate", 1714376788030L);
        verify(dataStorageMock, times(1)).addPatientData(2, 120.0, "BloodPressure", 1714376788031L);
    }

    @Test
    void readDataWithInvalidDirectory() {
        Path invalidDir = Paths.get("invalidDir");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new FileDataReader(invalidDir.toString()).readData(dataStorageMock);
        });

        assertEquals("Specified path is not a directory: invalidDir", exception.getMessage());
    }

    @Test
    void parseAndStoreDataWithMalformedData() throws IOException {
        Path tempFile = Files.createTempFile(tempDirectory, "data", ".txt");
        Files.write(tempFile, List.of("1,100.0,HeartRate", "2,120.0,BloodPressure,1714376788031"));

        dataReader.parseFile(tempFile.toFile(), dataStorageMock);

        verify(dataStorageMock, never()).addPatientData(eq(1), anyDouble(), anyString(), anyLong());
        verify(dataStorageMock, times(1)).addPatientData(2, 120.0, "BloodPressure", 1714376788031L);
    }

    @Test
    void parseAndStoreDataWithValidData() throws IOException {
        Path tempFile = Files.createTempFile(tempDirectory, "data", ".txt");
        Files.write(tempFile, List.of("1,100.0,HeartRate,1714376788030", "2,120.0,BloodPressure,1714376788031"));

        dataReader.parseFile(tempFile.toFile(), dataStorageMock);

        verify(dataStorageMock, times(1)).addPatientData(1, 100.0, "HeartRate", 1714376788030L);
        verify(dataStorageMock, times(1)).addPatientData(2, 120.0, "BloodPressure", 1714376788031L);
    }
}
