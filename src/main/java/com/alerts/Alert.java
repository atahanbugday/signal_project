package com.alerts;

/**
 * Represents an alert for a patient's health condition.
 */
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Constructs an Alert instance.
     *
     * @param patientId The ID of the patient.
     * @param condition Description of the alert condition.
     * @param timestamp The time when the alert was generated.
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getCondition() {
        return condition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "patientId='" + patientId + '\'' +
                ", condition='" + condition + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
