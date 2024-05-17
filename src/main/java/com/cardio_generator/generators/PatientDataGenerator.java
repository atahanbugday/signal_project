package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Creates an interface for data generator
 */

public interface PatientDataGenerator {

/**
 * @param patientId the ID of an individual patient
 * @param outputStrategy  the output strategy to configure output
 */

    void generate(int patientId, OutputStrategy outputStrategy);
}
