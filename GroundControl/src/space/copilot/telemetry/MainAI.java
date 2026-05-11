package space.copilot.telemetry;

public class MainAI {
    public static void main(String[] args) {
        String telemetryPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\telemetria.csv";
        String commandPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\sterowanie.csv";
        String modelSavePath = "rocket_model.zip";

        CommandWriterAI writer = new CommandWriterAI();
        TelemetryReader reader = new TelemetryReader();

        try {
            writer.loadAI(modelSavePath);
            System.out.println("Oczekiwanie na zapłon w KSP...");

            while (true) {
                var flightData = reader.readLog(telemetryPath);
                if (!flightData.isEmpty()) {
                    var latestData = flightData.get(flightData.size() - 1);

                    writer.writeCommand(latestData.altitude(), commandPath);

                    if (latestData.altitude() > 50000) {
                        System.out.println("MECO (Main Engine Cut Off) - Koniec nadawania.");
                        break;
                    }
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
