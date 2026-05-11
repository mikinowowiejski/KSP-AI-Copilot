package space.copilot.telemetry;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("====== SYSTEM GROUND CONTROL ZAINICJOWANY ======");
        String telemetryPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\telemetria.csv";
        String commandPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\sterowanie.csv";

        TelemetryReader reader = new TelemetryReader();
        CommandWriter writer = new CommandWriter();

        while (true) {
            try {
                List<TelemetryPoint> flightData = reader.readLog(telemetryPath);

                if (!flightData.isEmpty()) {
                    TelemetryPoint latestData = flightData.get(flightData.size() - 1);
                    writer.writeCommand(latestData.altitude(), commandPath);

                    if (latestData.altitude() > 50000) {
                        System.out.println("====== MECO: OPUSZCZONO ATMOSFERĘ ======");
                        System.out.println("Misja zakończona sukcesem. Autopilot wyłączony.");
                        break;
                    }

                }

                Thread.sleep(500);

            } catch (IOException e) {
                System.out.println("[Oczekiwanie na strumień danych...]");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
            } catch (InterruptedException e) {
                System.out.println("Pętla przerwana przez operatora.");
                break;
            }
        }

    }
}

