package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

class PatientTest {
    private Patient patient;

    @BeforeEach
    void init() {
        patient = new Patient(1);
    }

    @Test
    void addAndRetrieveRecords() {
        long now = System.currentTimeMillis();
        patient.addRecord(80.0, "HeartRate", now);
        patient.addRecord(100.0, "HeartRate", now + 1000);

        List<PatientRecord> records = patient.getRecords(now, now + 2000);
        assertEquals(2, records.size());
        assertEquals(80.0, records.get(0).getMeasurementValue());
        assertEquals(100.0, records.get(1).getMeasurementValue());
    }

    @Test
    void getRecordsWithNoMatches() {
        long now = System.currentTimeMillis();
        patient.addRecord(100.0, "HeartRate", now);

        List<PatientRecord> records = patient.getRecords(now + 1000, now + 2000);
        assertTrue(records.isEmpty());
    }

    @Test
    void getRecordsEdgeCases() {
        long now = System.currentTimeMillis();
        patient.addRecord(80.0, "HeartRate", now);
        patient.addRecord(100.0, "HeartRate", now + 1000);

        // Test with exact start time match
        List<PatientRecord> records = patient.getRecords(now, now + 500);
        assertEquals(1, records.size());
        assertEquals(80.0, records.get(0).getMeasurementValue());

        // Test with exact end time match
        records = patient.getRecords(now + 500, now + 1000);
        assertEquals(1, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }
}
