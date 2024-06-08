package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AlertGenerator is responsible for evaluating patient data and generating alerts based on certain medical conditions.
 */
public class AlertGenerator {
    private DataStorage storageSystem;
    private List<Alert> alerts;

    /**
     * Constructor to initialize AlertGenerator with the given DataStorage.
     *
     * @param storageSystem The DataStorage instance used to retrieve patient records.
     */
    public AlertGenerator(DataStorage storageSystem) {
        this.storageSystem = storageSystem;
        this.alerts = new ArrayList<>();
    }

    /**
     * Evaluates data for the given patient and checks for various conditions to generate alerts.
     *
     * @param patient The patient whose data is to be evaluated.
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
     * Checks for hypotensive hypoxemia condition (low blood pressure and low oxygen saturation).
     *
     * @param patient The patient to check for this condition.
     */
    private void checkHypotensiveHypoxemia(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = storageSystem.getRecords(patient.getPatientId(), currentTime - 600000, currentTime);

        boolean lowBP = records.stream()
                .anyMatch(r -> "SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < 90);
        boolean lowOxygen = records.stream()
                .anyMatch(r -> "Saturation".equals(r.getRecordType()) && r.getMeasurementValue() < 92);

        if (lowBP && lowOxygen) {
            generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia Alert", currentTime));
        }
    }

    /**
     * Checks for blood pressure conditions and generates alerts if necessary.
     *
     * @param patient The patient whose blood pressure is to be checked.
     */
    private void checkBloodPressure(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000;

        List<PatientRecord> systolicRecords = getRecords(patient, oneDayAgo, currentTime, "SystolicPressure");
        List<PatientRecord> diastolicRecords = getRecords(patient, oneDayAgo, currentTime, "DiastolicPressure");

        evaluatePressureAlerts(systolicRecords, "Systolic", currentTime, patient);
        evaluatePressureAlerts(diastolicRecords, "Diastolic", currentTime, patient);
    }

    /**
     * Retrieves patient records filtered by type and time range, and sorts them in reverse chronological order.
     *
     * @param patient    The patient whose records are to be retrieved.
     * @param startTime  The start of the time range.
     * @param endTime    The end of the time range.
     * @param recordType The type of record to filter by.
     * @return A list of filtered and sorted PatientRecord objects.
     */
    private List<PatientRecord> getRecords(Patient patient, long startTime, long endTime, String recordType) {
        return storageSystem.getRecords(patient.getPatientId(), startTime, endTime).stream()
                .filter(r -> recordType.equals(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Evaluates blood pressure records and generates alerts for critical pressures or trends.
     *
     * @param records    The list of blood pressure records.
     * @param type       The type of blood pressure (Systolic/Diastolic).
     * @param currentTime The current time for timestamping alerts.
     * @param patient    The patient whose records are being evaluated.
     */
    private void evaluatePressureAlerts(List<PatientRecord> records, String type, long currentTime, Patient patient) {
        if (records.isEmpty())
            return;

        records.forEach(record -> {
            if (isCriticalPressure(record, type)) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical " + type + " Pressure Alert", record.getTimestamp()));
            }
        });

        if (records.size() >= 3) {
            if (hasPressureTrend(records, true)) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), type + " Pressure Increasing Trend Alert", currentTime));
            } else if (hasPressureTrend(records, false)) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), type + " Pressure Decreasing Trend Alert", currentTime));
            }
        }
    }

    /**
     * Checks if the given record has a critical blood pressure value.
     *
     * @param record The patient record to check.
     * @param type   The type of blood pressure (Systolic/Diastolic).
     * @return True if the blood pressure value is critical, otherwise false.
     */
    private boolean isCriticalPressure(PatientRecord record, String type) {
        return ("Systolic".equals(type) && (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90)) ||
                ("Diastolic".equals(type) && (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60));
    }

    /**
     * Checks if the blood pressure records show a consistent trend (increasing or decreasing).
     *
     * @param records    The list of blood pressure records.
     * @param increasing True to check for an increasing trend, false for decreasing.
     * @return True if the records show the specified trend, otherwise false.
     */
    private boolean hasPressureTrend(List<PatientRecord> records, boolean increasing) {
        for (int i = 0; i < records.size() - 1; i++) {
            if (increasing && records.get(i).getMeasurementValue() - records.get(i + 1).getMeasurementValue() <= 10)
                return false;
            if (!increasing && records.get(i + 1).getMeasurementValue() - records.get(i).getMeasurementValue() <= 10)
                return false;
        }
        return true;
    }

    /**
     * Checks for oxygen level conditions and generates alerts if necessary.
     *
     * @param patient The patient whose oxygen levels are to be checked.
     */
    private void checkOxygenLevels(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = getRecords(patient, currentTime - 600000, currentTime, "Saturation");

        records.stream()
                .filter(r -> r.getMeasurementValue() < 92)
                .findFirst()
                .ifPresent(record -> generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Low Saturation Alert", record.getTimestamp())));

        for (int i = 1; i < records.size(); i++) {
            if (100.0 * (records.get(i - 1).getMeasurementValue() - records.get(i).getMeasurementValue()) / records.get(i - 1).getMeasurementValue() >= 5) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Rapid Oxygen Drop Alert", records.get(i).getTimestamp()));
                break;
            }
        }
    }

    /**
     * Checks for abnormal ECG readings and generates alerts if necessary.
     *
     * @param patient The patient whose ECG readings are to be checked.
     */
    private void checkECGReadings(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> ecgRecords = getRecords(patient, currentTime - 3600000, currentTime, "ECG");

        ecgRecords.stream()
                .filter(record -> record.getMeasurementValue() < 50 || record.getMeasurementValue() > 100)
                .findFirst()
                .ifPresent(record -> generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Abnormal Heart Rate Alert", record.getTimestamp())));

        double avgInterval = computeAverageInterval(ecgRecords);
        double allowableDeviation = avgInterval * 0.1;

        for (int i = 1; i < ecgRecords.size(); i++) {
            long intervalDiff = Math.abs(ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp());
            if (Math.abs(intervalDiff - avgInterval) > allowableDeviation) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Irregular Beat Alert", ecgRecords.get(i).getTimestamp()));
                break;
            }
        }
    }

    /**
     * Computes the average interval between consecutive ECG records.
     *
     * @param ecgRecords The list of ECG records.
     * @return The average interval in milliseconds.
     */
    private double computeAverageInterval(List<PatientRecord> ecgRecords) {
        long totalInterval = 0;
        for (int i = 1; i < ecgRecords.size(); i++) {
            totalInterval += ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp();
        }
        return (double) totalInterval / (ecgRecords.size() - 1);
    }

    /**
     * Generates an alert and adds it to the list of alerts.
     *
     * @param alert The alert to be generated.
     */
    private void generateAlert(Alert alert) {
        alerts.add(alert);
        System.out.println("Alert triggered: " + alert.getCondition() + " for patient " + alert.getPatientId() + " at " + alert.getTimestamp());
    }

    /**
     * Returns the list of generated alerts.
     *
     * @return A list of alerts.
     */
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }
}
