package space.copilot.telemetry;

public class EvolutionMain {
    public static void main(String[] args) {
        System.out.println("=== STARTING HYBRID EVOLUTION SYSTEM ===");

        EvolutionTrainer trainer = new EvolutionTrainer();

        String baseModel = "rocket_model.zip";
        int generations = 30;
        int childrenPerGen = 5;

        try {
            trainer.runEvolution(baseModel, generations, childrenPerGen);

        } catch (Exception e) {
            System.err.println("Krytyczny błąd ewolucji: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
