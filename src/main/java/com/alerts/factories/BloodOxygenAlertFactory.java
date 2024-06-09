package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BloodOxygenAlert;

/**
 * Factory class for creating BloodOxygenAlert instances.
 */
public class BloodOxygenAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String condition, String patientId, long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}
