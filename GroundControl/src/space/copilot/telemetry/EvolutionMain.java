package space.copilot.telemetry;

public class EvolutionMain {
    public static void main(String[] args) {
        System.out.println("=== STARTING HYBRID EVOLUTION SYSTEM ===");

        EvolutionTrainer trainer = new EvolutionTrainer();

        String baseModel = "absolute_master.zip";
        int generations = 100;
        int childrenPerGen = 25;

        try {
            trainer.runEvolution(baseModel, generations, childrenPerGen);

        } catch (Exception e) {
            System.err.println("Krytyczny błąd ewolucji: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
