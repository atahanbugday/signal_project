import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.data_management.PatientRecord;


public class FileDataReader implements DataReader{
    
    //To-Do: Add explanation of the code
    public void readData(String outputDirectory, DataStorage dataStorage)
    {
        File dir=new File(outputDirectory);
        if(!dir.exists() || !dir.isDirectory())
        {
            throw new IllegalArgumentException("Invalid argument!"+outputDirectory);
        }

        File[] files = dir.listFiles((d,name)->name.endsWith(".txt"));

        if(files==null)
        {
            throw new IllegalArgumentException("No files in the directory!"+outputDirectory);
        }

        for(File file:files)
        {
            parseFile(file,dataStorage);
            }
        

    }


    private void parseFile(File file, DataStorage dataStorage)
    {
        try(BufferedReader reader=new BufferedReader(new FileReader(file)))
        {
            String line;

            while((line=reader.readLine()!=null))
            {
                PatientRecord record=parseLineToPatientRecord(line);
               if( record!=null)
               {
                dataStorage.addPatientData(record.getPatientId(),record.getMeasurementValue(),record.getRecordType(),record.getTimestamp());
               }
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    private PatientRecord parseLineToPatientRecord(String line)
    {
        String [] parts=line.split(",");
        String [] patientIDPart=parts[0].split(":");
        String [] timeStampPart=parts[1].split(":");
        String [] labelPart=parts[2].split(":");
        String [] dataPart=parts[3].split(":");

        try{
        int patientID=Int.parse(patientIDPart[1]);
        long timeStamp=Long.parseLong(timeStampPart[1]);
        String recordType=labelPart[1];
        double measurementValue=Double.parseDouble(dataPart[1]);

        return new PatientRecord(patientID, measurementValue, recordType, timeStamp);
        }catch(NumberFormatException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}