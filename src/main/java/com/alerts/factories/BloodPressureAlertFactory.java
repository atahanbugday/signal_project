package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BloodPressureAlert;

/**
 * Factory class for creating BloodPressureAlert instances.
 */
public class BloodPressureAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String condition, String patientId, long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }
}
