package com.alerts;

/**
 * Represents a blood pressure alert.
 */
public class BloodPressureAlert extends Alert {
    /**
     * Constructs a BloodPressureAlert instance.
     *
     * @param patientId The ID of the patient.
     * @param condition Description of the alert condition.
     * @param timestamp The time when the alert was generated.
     */
    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String toString() {
        return "BloodPressureAlert{" +
                "patientId='" + getPatientId() + '\'' +
                ", condition='" + getCondition() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
