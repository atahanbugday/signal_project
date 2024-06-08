package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates medical alerts by analyzing patient health data. This class evaluates indicators like blood pressure, oxygen levels, and ECG readings to identify urgent health conditions.
 */
public class AlertGenerator {
    private DataStorage storageSystem;
    private AlertStrategy alertProtocol;

    /**
     * Constructs an AlertGenerator with the specified data storage system.
     *
     * @param storageSystem the data storage system used to retrieve patient information
     */
    public AlertGenerator(DataStorage storageSystem) {
        this.storageSystem = storageSystem;
    }

    /**
     * Configures the alert strategy to be utilized.
     *
     * @param alertProtocol the alert strategy to apply
     */
    public void configureAlertProtocol(AlertStrategy alertProtocol) {
        if (alertProtocol == null) {
            throw new NullPointerException("Alert strategy cannot be null.");
        }
        this.alertProtocol = alertProtocol;
        
    }

    /**
     * Analyzes patient data to check for alert conditions.
     *
     * @param patient the patient data to analyze for alerts
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            throw new NullPointerException("No patient data available.");
        }

        checkBloodPressure(patient);
        checkOxygenLevels(patient);
        checkECGReadings(patient);
        checkHypotensiveHypoxemia(patient);
    }

    /**
     * Analyzes patient data using the strategy pattern for alert conditions.
     *
     * @param patient the patient data to evaluate
     */
    public void analyzeDataWithStrategy(Patient patient) {
        if (patient == null || alertProtocol == null) {
            throw new NullPointerException("Patient data or alert strategy cannot be null.");
        }
        alertProtocol.checkAlert(patient);
    }

    /**
     * Analyzes blood pressure and oxygen levels to detect hypotensive hypoxemia.
     *
     * @param patient the patient to analyze
     */
    private void checkHypotensiveHypoxemia(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> recentRecords = storageSystem.getRecords(patient.getPatientId(), currentTime - 600000, currentTime);

        boolean lowBP = recentRecords.stream().anyMatch(r -> "SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < 90);
        boolean lowOxygen = recentRecords.stream().anyMatch(r -> "Saturation".equals(r.getRecordType()) && r.getMeasurementValue() < 92);

        if (lowBP && lowOxygen) {
            generateAlert(new Alert(Integer.toString(patient.getPatientId()), "Hypotensive Hypoxemia Alert", currentTime));
        }
    }

    /**
     * Analyzes blood pressure data for critical conditions or trends.
     *
     * @param patient the patient whose blood pressure data is being monitored
     */
    private void checkBloodPressure(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000;

        List<PatientRecord> systolicRecords = getSortedRecords(patient, oneDayAgo, currentTime, "SystolicPressure");
        List<PatientRecord> diastolicRecords = getSortedRecords(patient, oneDayAgo, currentTime, "DiastolicPressure");

        evaluatePressureAlerts(systolicRecords, "Systolic", currentTime, patient);
        evaluatePressureAlerts(diastolicRecords, "Diastolic", currentTime, patient);
    }

    private List<PatientRecord> getRecords(Patient patient, long startTime, long endTime, String recordType) {
        return storageSystem.getRecords(patient.getPatientId(), startTime, endTime).stream()
                .filter(r -> recordType.equals(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    private void evaluatePressureAlerts(List<PatientRecord> records, String type, long currentTime, Patient patient) {
        if (records.isEmpty()) return;

        records.forEach(record -> {
            if (isCriticalPressure(record, type)) {
                generateAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical " + type + " Pressure Alert", record.getTimestamp()));
            }
        });

        if (records.size() >= 3 && hasPressureTrend(records, true)) {
            generateAlert(new Alert(Integer.toString(patient.getPatientId()), type + " Pressure Increasing Trend Alert", currentTime));
        } else if (hasPressureTrend(records, false)) {
            generateAlert(new Alert(Integer.toString(patient.getPatientId()), type + " Pressure Decreasing Trend Alert", currentTime));
        }
    }

    private boolean isCriticalPressure(PatientRecord record, String type) {
        return (type.equals("Systolic") && (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90)) ||
               (type.equals("Diastolic") && (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60));
    }

    private boolean hasPressureTrend(List<PatientRecord> records, boolean increasing) {
        for (int i = 0; i < records.size() - 1; i++) {
            if (increasing) {
                if (records.get(i).getMeasurementValue() - records.get(i + 1).getMeasurementValue() <= 10) return false;
            } else {
                if (records.get(i + 1).getMeasurementValue() - records.get(i).getMeasurementValue() <= 10) return false;
            }
        }
        return true;
    }

    /**
     * Analyzes oxygen saturation levels for critical drops or low levels.
     *
     * @param patient the patient whose oxygen levels are monitored
     */
    private void checkOxygenLevels(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = getSortedRecords(patient, currentTime - 600000, currentTime, "Saturation");

        records.stream().filter(r -> r.getMeasurementValue() < 92).findFirst()
                .ifPresent(record -> generateAlert(new Alert(Integer.toString(patient.getPatientId()), "Low Saturation Alert", record.getTimestamp())));

        for (int i = 1; i < records.size(); i++) {
            if (100.0 * (records.get(i - 1).getMeasurementValue() - records.get(i).getMeasurementValue()) / records.get(i - 1).getMeasurementValue() >= 5) {
                generateAlert(new Alert(Integer.toString(patient.getPatientId()), "Rapid Oxygen Drop Alert", records.get(i).getTimestamp()));
                break;
            }
        }
    }

    /**
     * Analyzes ECG data for abnormal heart rates or irregular beat patterns.
     *
     * @param patient the patient whose ECG data is being analyzed
     */
    private void checkECGReadings(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> ecgRecords = getSortedRecords(patient, currentTime - 3600000, currentTime, "ECG");

        ecgRecords.stream().filter(record -> record.getMeasurementValue() < 50 || record.getMeasurementValue() > 100).findFirst()
                .ifPresent(record -> generateAlert(new Alert(Integer.toString(patient.getPatientId()), "Abnormal Heart Rate Alert", record.getTimestamp())));

        double avgInterval = computeAverageInterval(ecgRecords);
        double allowableDeviation = avgInterval * 0.1;

        for (int i = 1; i < ecgRecords.size(); i++) {
            long intervalDiff = Math.abs(ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp());
            if (Math.abs(intervalDiff - avgInterval) > allowableDeviation) {
                generateAlert(new Alert(Integer.toString(patient.getPatientId()), "Irregular Beat Alert", ecgRecords.get(i).getTimestamp()));
                break;
            }
        }
    }

    private double computeAverageInterval(List<PatientRecord> ecgRecords) {
        long totalInterval = 0;
        for (int i = 1; i < ecgRecords.size(); i++) {
            totalInterval += (ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp());
        }
        return totalInterval / (double)(ecgRecords.size() - 1);
    }

    /**
     * Triggers an alert and logs its details.
     *
     * @param alert the alert to trigger
     */
    private void generateAlert(Alert alert) {
        System.out.println("Alert triggered: " + alert.getCondition() + " for patient " + alert.getPatientId() + " at " + alert.getTimestamp());
    }
}
