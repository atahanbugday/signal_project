package com.data_management;

import java.io.IOException;

public interface DataReader {
    /**
     * Reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage the storage where data will be stored
     * @param outputDirectory the directory that data will be read
     * @throws IOException if there is an error reading the data
     */
    void readData(String outputDirectory,DataStorage dataStorage) throws IOException;
}
