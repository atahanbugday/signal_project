package com.alerts.factories;

import com.alerts.Alert;

/**
 * Abstract factory class for creating alerts.
 */
public abstract class AlertFactory {
    /**
     * Creates an Alert instance based on the condition.
     *
     * @param condition Description of the alert condition.
     * @param patientId The ID of the patient.
     * @param timestamp The time when the alert was generated.
     * @return An instance of Alert.
     */
    public abstract Alert createAlert(String condition, String patientId, long timestamp);
}
