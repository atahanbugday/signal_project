package com.yourpackage;

import java.io.IOException;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;

/**
 * The main entry point for the application. This class select which class to run based on
 * command-line parameters: DataStorage or the HealthDataSimulator
 */
public class Main {
    /**
     * The main method to run the application.
     *
     * @param args command line arguments 
     * @throws IOException if an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage.main(new String[]{});
        } else {
            HealthDataSimulator.getInstance().startSimulation(args);
        }
    }
}