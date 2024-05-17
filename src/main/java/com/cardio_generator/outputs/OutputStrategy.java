package com.cardio_generator.outputs;


/**
 * Interface to output data
 * Provides to output data to console/file/tcp/websocket
 */

public interface OutputStrategy {

/**
 * @param patientID ID for an individual patient (IDs are unique)
 * @param timestamp the time when the data is recorded
 * @param label shows the type of data (HeartRate etc.)
 * @param data the data to output
 */

    void output(int patientId, long timestamp, String label, String data);
}
