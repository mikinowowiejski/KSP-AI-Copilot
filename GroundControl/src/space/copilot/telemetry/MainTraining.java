package space.copilot.telemetry;

public class MainTraining {
    public static void main(String[] args) {
        try {
            // 1. Ścieżki
            String csvPath = "training_data.csv";
            String modelSavePath = "rocket_model.zip";

            // 2. Inicjalizacja trenera
            TelemetryTrainer trainer = new TelemetryTrainer();

            // 3. START NAUKI
            System.out.println("Zaczynamy sesję nauki AI...");
            trainer.trainModel(csvPath, modelSavePath);
            System.out.println("Naukę zakończono! Mamy gotowy model.");

        } catch (Exception e) {
            System.err.println("Błąd podczas treningu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
