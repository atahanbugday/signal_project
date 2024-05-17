package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * This class implements PatientDataGenerator to generate alerts for patients
 * Simulates alert conditions to monitor 
 */

public class AlertGenerator implements PatientDataGenerator {

    public static final Random randomGenerator = new Random();
    private boolean[] alertStates; // false = resolved, true = pressed //changed to camelCase


/**Constructor method to initialize alert states for each patient. 
 * @param patientCount  the number of the patients
 */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1]; //changed to camelCase
    }


    /**
     * Generates an alert (resolved or trigged) based on probability and current state.
     * @param patientId Unique ID of a patient
     * @param outputStrategy output strategy to output alert data
     */

    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) { //changed to camelCase
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false; //changed to camelCase
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                double Lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-Lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) { 
                    alertStates[patientId] = true; // changed to camelCase
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
