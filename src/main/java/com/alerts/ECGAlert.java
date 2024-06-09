package com.alerts;

/**
 * Represents an ECG alert.
 */
public class ECGAlert extends Alert {
    /**
     * Constructs an ECGAlert instance.
     *
     * @param patientId The ID of the patient.
     * @param condition Description of the alert condition.
     * @param timestamp The time when the alert was generated.
     */
    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String toString() {
        return "ECGAlert{" +
                "patientId='" + getPatientId() + '\'' +
                ", condition='" + getCondition() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
