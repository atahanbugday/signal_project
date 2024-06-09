package com.alerts;

/**
 * Represents a blood oxygen alert.
 */
public class BloodOxygenAlert extends Alert {
    /**
     * Constructs a BloodOxygenAlert instance.
     *
     * @param patientId The ID of the patient.
     * @param condition Description of the alert condition.
     * @param timestamp The time when the alert was generated.
     */
    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String toString() {
        return "BloodOxygenAlert{" +
                "patientId='" + getPatientId() + '\'' +
                ", condition='" + getCondition() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
