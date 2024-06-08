package alerts;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AlertGeneratorTest {
    private DataStorage dataStorageMock;
    private AlertGenerator generator;

    @BeforeEach
    void init() {
        dataStorageMock = mock(DataStorage.class);
        generator = new AlertGenerator(dataStorageMock);
    }

    @Test
    void testTriggerHighHeartRateAlert() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List.of(new PatientRecord(1, 110.0, "ECG", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertEquals(1, alerts.size());
        assertEquals("Abnormal Heart Rate Alert", alerts.get(0).getCondition());
    }

    @Test
    void testNotAlertForNormalHeartRate() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List.of(new PatientRecord(1, 80.0, "HeartRate", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertTrue(alerts.isEmpty());
    }

    @Test
    void testNotAlertWhenNoRecords() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(List.of());

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertTrue(alerts.isEmpty());
    }

    @Test
    void testAlertForLowBloodPressure() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List
                .of(new PatientRecord(1, 70.0, "SystolicPressure", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertEquals(1, alerts.size());
        assertEquals("Critical Systolic Pressure Alert", alerts.get(0).getCondition());
    }

    @Test
    void testAlertForLowBloodSaturation() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List.of(new PatientRecord(1, 89.0, "Saturation", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertEquals(1, alerts.size());
        assertEquals("Low Saturation Alert", alerts.get(0).getCondition());
    }

    @Test
    void testDetectAbnormalECG() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List.of(new PatientRecord(1, 2.0, "ECG", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertEquals(1, alerts.size());
        assertEquals("Abnormal Heart Rate Alert", alerts.get(0).getCondition());
    }

    @Test
    void testGenerateAlertsForMultipleConditionsInOneRecord() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 105.0, "ECG", System.currentTimeMillis()),
                new PatientRecord(1, 75.0, "SystolicPressure", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertEquals(2, alerts.size());
        assertEquals("Critical Systolic Pressure Alert", alerts.get(0).getCondition());
        assertEquals("Abnormal Heart Rate Alert", alerts.get(1).getCondition());
    }

    @Test
    void testNotAlertForBorderlineValues() {
        Patient patientMock = mock(Patient.class);
        when(patientMock.getPatientId()).thenReturn(1);
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 100.0, "HeartRate", System.currentTimeMillis()),
                new PatientRecord(1, 140.0, "SystolicPressure", System.currentTimeMillis()));
        when(dataStorageMock.getRecords(anyInt(), anyLong(), anyLong())).thenReturn(records);

        generator.evaluateData(patientMock);

        List<Alert> alerts = generator.getAlerts();
        assertTrue(alerts.isEmpty());
    }

}
