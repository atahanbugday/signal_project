package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements OutputStrategy for file output
 */

public class fileOutputStrategy implements OutputStrategy {

    private String baseDirectory; // changed variable name to camelCase 

    // changed variable name to UPPER_SNAKE_CASE since it is a constant
    public final ConcurrentHashMap<String, String> FILE_MAP = new ConcurrentHashMap<>(); 


/**
 * Constructor method
 * it sets base directory for file output
 * @param baseDirectory the base directory path
 */
    public fileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory; // changed name to camelCase
    }


/**
 * Organizes data by labels and outputs formatted data to files
 * @param patientID the ID of the patient
 * @param timestamp the time when the data recorded 
 * @param label the data label
 * @param data the data to output
 */


    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(BaseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // changed variable name to UPPER_SNAKE_CASE since it is a constant
        //if file path is absent compute it, otherwise retrieve it
        String FilePath = FILE_MAP.computeIfAbsent(label, k -> Paths.get(BaseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(FilePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            //Use System.err.println to log errors
            System.err.println("Error writing to file " + FilePath + ": " + e.getMessage());
        }
    }
}