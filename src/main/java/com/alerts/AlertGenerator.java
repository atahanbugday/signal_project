package com.alerts;

import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates alerts based on patient data.
 */
public class AlertGenerator {
    private DataStorage storageSystem;
    private List<Alert> alerts;
    private AlertFactory bloodPressureFactory;
    private AlertFactory bloodOxygenFactory;
    private AlertFactory ecgFactory;

    /**
     * Constructs an AlertGenerator with the given DataStorage instance.
     * 
     * @param storageSystem the data storage system
     */
    public AlertGenerator(DataStorage storageSystem) {
        this.storageSystem = storageSystem;
        this.alerts = new ArrayList<>();
        this.bloodPressureFactory = new BloodPressureAlertFactory();
        this.bloodOxygenFactory = new BloodOxygenAlertFactory();
        this.ecgFactory = new ECGAlertFactory();
    }

    /**
     * Evaluates the patient data and generates alerts based on various conditions.
     * 
     * @param patient the patient whose data is being evaluated
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
     * Checks the patient's records for signs of hypotensive hypoxemia and generates
     * an alert if conditions are met.
     * 
     * @param patient the patient to check
     */
    private void checkHypotensiveHypoxemia(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = storageSystem.getRecords(patient.getPatientId(), currentTime - 600000,
                currentTime);

        boolean lowBP = records.stream()
                .anyMatch(r -> "SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < 90);
        boolean lowOxygen = records.stream()
                .anyMatch(r -> "Saturation".equals(r.getRecordType()) && r.getMeasurementValue() < 92);

        if (lowBP && lowOxygen) {
            generateAlert(
                    new Alert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia Alert", currentTime));
        }
    }

    /**
     * Checks the patient's blood pressure records and generates alerts based on
     * critical values or trends.
     * 
     * @param patient the patient whose blood pressure is being checked
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
     * Retrieves records of a specific type for a given patient within a time range.
     * 
     * @param patient    the patient whose records are being retrieved
     * @param startTime  the start time of the range
     * @param endTime    the end time of the range
     * @param recordType the type of record to retrieve
     * @return a list of matching records
     */
    private List<PatientRecord> getRecords(Patient patient, long startTime, long endTime, String recordType) {
        return storageSystem.getRecords(patient.getPatientId(), startTime, endTime).stream()
                .filter(r -> recordType.equals(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Evaluates the patient's blood pressure records for critical values and
     * trends, generating alerts accordingly.
     * 
     * @param records     the list of patient records
     * @param type        the type of pressure (Systolic/Diastolic)
     * @param currentTime the current time for alert timestamps
     * @param patient     the patient whose records are being checked
     */
    private void evaluatePressureAlerts(List<PatientRecord> records, String type, long currentTime, Patient patient) {
        if (records.isEmpty())
            return;

        records.forEach(record -> {
            if (isCriticalPressure(record, type)) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical " + type + " Pressure Alert",
                        record.getTimestamp()));
            }
        });

        if (records.size() >= 3) {
            if (hasPressureTrend(records, true)) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()),
                        type + " Pressure Increasing Trend Alert", currentTime));
            } else if (hasPressureTrend(records, false)) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()),
                        type + " Pressure Decreasing Trend Alert", currentTime));
            }
        }
    }

    /**
     * Determines if a given record indicates critical blood pressure values.
     * 
     * @param record the patient record
     * @param type   the type of pressure (Systolic/Diastolic)
     * @return true if the record indicates critical values, false otherwise
     */
    private boolean isCriticalPressure(PatientRecord record, String type) {
        return ("Systolic".equals(type) && (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90)) ||
                ("Diastolic".equals(type) && (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60));
    }

    /**
     * Determines if the patient's blood pressure records show a trend of increasing
     * or decreasing values.
     * 
     * @param records    the list of records
     * @param increasing true to check for increasing trend, false for decreasing
     *                   trend
     * @return true if a significant trend is detected, false otherwise
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
     * Checks the patient's oxygen saturation records and generates alerts for low
     * levels or rapid drops.
     * 
     * @param patient the patient whose oxygen levels are being checked
     */
    private void checkOxygenLevels(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = getRecords(patient, currentTime - 600000, currentTime, "Saturation");

        records.stream()
                .filter(r -> r.getMeasurementValue() < 92)
                .findFirst()
                .ifPresent(record -> generateAlert(new Alert(String.valueOf(patient.getPatientId()),
                        "Low Saturation Alert", record.getTimestamp())));

        for (int i = 1; i < records.size(); i++) {
            if (100.0 * (records.get(i - 1).getMeasurementValue() - records.get(i).getMeasurementValue())
                    / records.get(i - 1).getMeasurementValue() >= 5) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Rapid Oxygen Drop Alert",
                        records.get(i).getTimestamp()));
                break;
            }
        }
    }

    /**
     * Checks the patient's ECG records for abnormal heart rates or irregular beats
     * and generates alerts accordingly.
     * 
     * @param patient the patient whose ECG readings are being checked
     */
    private void checkECGReadings(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> ecgRecords = getRecords(patient, currentTime - 3600000, currentTime, "ECG");

        ecgRecords.stream()
                .filter(record -> record.getMeasurementValue() < 50 || record.getMeasurementValue() > 100)
                .findFirst()
                .ifPresent(record -> generateAlert(new Alert(String.valueOf(patient.getPatientId()),
                        "Abnormal Heart Rate Alert", record.getTimestamp())));

        double avgInterval = computeAverageInterval(ecgRecords);
        double allowableDeviation = avgInterval * 0.1;

        for (int i = 1; i < ecgRecords.size(); i++) {
            long intervalDiff = Math.abs(ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp());
            if (Math.abs(intervalDiff - avgInterval) > allowableDeviation) {
                generateAlert(new Alert(String.valueOf(patient.getPatientId()), "Irregular Beat Alert",
                        ecgRecords.get(i).getTimestamp()));
                break;
            }
        }
    }

    /**
     * Computes the average interval between consecutive ECG records.
     * 
     * @param ecgRecords the list of ECG records
     * @return the average interval in milliseconds
     */
    private double computeAverageInterval(List<PatientRecord> ecgRecords) {
        long totalInterval = 0;
        for (int i = 1; i < ecgRecords.size(); i++) {
            totalInterval += ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp();
        }
        return (double) totalInterval / (ecgRecords.size() - 1);
    }

    /**
     * Generates and stores an alert.
     * 
     * @param alert the alert to be generated
     */
    private void generateAlert(Alert alert) {
        alerts.add(alert);
        System.out.println("Alert triggered: " + alert.getCondition() + " for patient " + alert.getPatientId() + " at "
                + alert.getTimestamp());
    }

    /**
     * Retrieves the list of generated alerts.
     * 
     * @return a list of alerts
     */
    public List<Alert> getAlerts() {
        return new ArrayList<>(alerts);
    }
}
