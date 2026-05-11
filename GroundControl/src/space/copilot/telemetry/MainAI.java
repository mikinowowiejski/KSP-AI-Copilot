package space.copilot.telemetry;

import java.nio.file.Files;
import java.nio.file.Path;

public class MainAI {
    public static void main(String[] args) {
        String telemetryPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\telemetria.csv";
        String commandPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\sterowanie.csv";
        String modelSavePath = "rocket_model.zip";

        CommandWriterAI writer = new CommandWriterAI();
        TelemetryReader reader = new TelemetryReader();


        try {
            writer.loadAI(modelSavePath);

            System.out.println("Oczekiwanie na nową rakietę na platformie startowej...");

            boolean isArmed = false;

            while (true) {
                var flightData = reader.readLog(telemetryPath);
                if (!flightData.isEmpty()) {
                    var latestData = flightData.get(flightData.size() - 1);
                    double currentAlt = latestData.altitude();

                    // System uzbraja się tylko na platformie startowej
                    if (!isArmed && currentAlt < 1000) {
                        System.out.println(">>> WYKRYTO RAKIETĘ NA STARCIE! SYSTEM AKTYWNY <<<");
                        isArmed = true;
                    }

                    if (isArmed) {
                        writer.writeCommand(currentAlt, commandPath);

                        if (currentAlt > 50000) {
                            System.out.println("KONIEC MISJI - OSIĄGNIĘTO PUŁAP OPERACYJNY.");
                            break;
                        }
                    }
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}