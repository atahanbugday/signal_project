package alerts;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the AlertFactory implementations.
 * This class contains test cases to verify the functionality of the alert
 * creation factories.
 */
public class AlertFactoryTest {

    /**
     * Tests the BloodOxygenAlertFactory by creating an alert and verifying its
     * attributes.
     */
    @Test
    public void testBloodOxygenAlertFactory() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("Low Oxygen", "1", 1627844930000L);

        assertNotNull(alert);
        assertEquals("1", alert.getPatientId());
        assertEquals("Low Oxygen", alert.getCondition());
        assertEquals(1627844930000L, alert.getTimestamp());
    }

    /**
     * Tests the BloodPressureAlertFactory by creating an alert and verifying its
     * attributes.
     */
    @Test
    public void testBloodPressureAlertFactory() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("High BLood Pressure", "2", 1627845952000L);

        assertNotNull(alert);
        assertEquals("2", alert.getPatientId());
        assertEquals("High BLood Pressure", alert.getCondition());
        assertEquals(1627845952000L, alert.getTimestamp());
    }

    /**
     * Tests the ECGAlertFactory by creating an alert and verifying its attributes.
     */
    @Test
    public void testECGAlertFactory() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("Abnormal ECG", "3", 1627840953000L);

        assertNotNull(alert);
        assertEquals("3", alert.getPatientId());
        assertEquals("Abnormal ECG", alert.getCondition());
        assertEquals(1627840953000L, alert.getTimestamp());
    }
}