package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileDataReader implements DataReader {

    private String dirPath;

    public FileDataReader(String tempDirectory) {
        this.dirPath = tempDirectory;
    }

    public void readData(DataStorage dataStorage) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Specified path is not a directory: " + dirPath);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt")); // working with text files

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files in the directory: " + dirPath);
        }

        for (File file : files) {
            parseFile(file, dataStorage);
        }
    }

    public void parseFile(File file, DataStorage dataStorage) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                PatientRecord record = parseLineToPatientRecord(line);
                if (record != null) {
                    dataStorage.addPatientData(record.getPatientId(), record.getMeasurementValue(),
                            record.getRecordType(), record.getTimestamp());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PatientRecord parseLineToPatientRecord(String line) {
        String[] parts = line.split(",");

        try {
            int patientId = Integer.parseInt(parts[0].trim());
            double measurementValue = Double.parseDouble(parts[1].trim());
            String recordType = parts[2].trim();
            long timestamp = Long.parseLong(parts[3].trim());

            return new PatientRecord(patientId, measurementValue, recordType, timestamp);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }
}
