package space.copilot.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class ResultReader {

    public ResultPoint readResult(String filePath) throws IOException
    {
       try(BufferedReader reader = Files.newBufferedReader(Path.of(filePath)))
       {
           String line = reader.readLine();
           if(line != null && !line.isBlank())
           {
               String[] parts = line.split(",");

               double maxAlt = Double.parseDouble(parts[0]);
               double apo = Double.parseDouble(parts[1]);
               double peri = Double.parseDouble(parts[2]);
               double finalFuel = Double.parseDouble(parts[3]);
               boolean isCrashed = parts[4].equals("1");

               return new ResultPoint(maxAlt,apo,peri,finalFuel,isCrashed);
           }

           return null;
       }
    }

}
