package space.copilot.telemetry;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class EvolutionTrainer {

    private static final double NOISE_SCALE = 0.05;
    private static final double MUTATION_RATE = 0.3;

    private final String telemetryPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\telemetria.csv";
    private final String commandPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\sterowanie.csv";
    private final String resultPath = "C:\\Program Files\\Epic Games\\KerbalSpaceProgram\\English\\Ships\\Script\\result.csv";

    // Moduły pomocnicze
    private final TelemetryReader telReader = new TelemetryReader();
    private final CommandWriterAI writer = new CommandWriterAI();
    private final ResultReader resReader = new ResultReader();
    private final RewardCalculator rewardCalc = new RewardCalculator();

    public MultiLayerNetwork loadBaseModel(String path) throws IOException
    {
        return MultiLayerNetwork.load(new File(path), true);
    }

    public void saveModel(MultiLayerNetwork model, String path) throws IOException
    {
        model.save(new File(path), true);
        System.out.println("zapisano nowego mistrza: " + path);
    }


    public List<MultiLayerNetwork> createMutants(MultiLayerNetwork parent, int numberOfChildren)
    {
        List<MultiLayerNetwork> children = new ArrayList<>();

        for(int i = 0; i < numberOfChildren; i++)
        {
            MultiLayerNetwork child = parent.clone();

            INDArray params = child.params();
            INDArray noise = Nd4j.randn(params.shape()).mul(NOISE_SCALE);

            INDArray mask = Nd4j.rand(params.shape()).lt(MUTATION_RATE);
            noise.muli(mask);

            params.addi(noise);
            child.setParams(params);
            children.add(child);
        }

        return children;
    }

    public void runEvolution(String baseModelPath, int generations, int childrenPerGen) throws Exception
    {
        MultiLayerNetwork currentBestModel = loadBaseModel(baseModelPath);

        for(int gen = 1; gen <= generations; gen++)
        {
            System.out.println("\n--- GENERACJA " + gen + "---");

            List<MultiLayerNetwork> children = createMutants(currentBestModel, childrenPerGen-1);

            children.add(0, currentBestModel.clone());

            MultiLayerNetwork bestChild = null;
            double bestScore = -999999.0;

            for(int i = 0; i < children.size(); i++)
            {
                if (i == 0) {
                    System.out.println(">> Lot Mistrza (Rodzica) z poprzedniej generacji... ");
                } else {
                    System.out.println(">> Lot mutanta nr " + i + "/" + (childrenPerGen - 1));
                }
                MultiLayerNetwork child = children.get(i);

                writer.setAiModel(child);

                double score = performFlightAndWaitForResult();

                if (i == 0) {
                    System.out.println("Mistrz zdobył: " + score + " pkt.");
                } else {
                    System.out.println("Mutant " + i + " zdobył: " + score + " pkt.");
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestChild = child;
                }
            }

            System.out.println("\nZwycięzca Generacji " + gen + " zdobył: " + bestScore + " pkt.");
            currentBestModel = bestChild; // Prawo dżungli: Zwycięzca zostaje nowym rodzicem
            saveModel(currentBestModel, "best_evolution_model.zip");

            logEvolutionStats(gen, bestScore);

            System.gc();
        }
    }

    private double performFlightAndWaitForResult() throws Exception
    {
        boolean isArmed = false;

        while(true)
        {
            var flightData = telReader.readLog(telemetryPath);

            if(!flightData.isEmpty())
            {
                var latestData = flightData.get(flightData.size() - 1);

                if (!isArmed && latestData.altitude() < 1000) {
                    System.out.println("   [KSP] Rakietę wykryto na wyrzutni. System uzbrojony.");
                    isArmed = true;
                }

                if (isArmed) {

                    writer.writeCommand(latestData.altitude(), latestData.velocity(), latestData.twr(), latestData.q(), latestData.apoapsis(), latestData.etaApo(), commandPath);

                    System.out.print("\r   -> TELEMETRIA: " + latestData.toString() + "      ");
                }

                Path resFile = Path.of(resultPath);
                if (Files.exists(resFile)) {

                    Thread.sleep(100);

                    ResultPoint result = resReader.readResult(resultPath);

                    if (result != null) {
                        System.out.println();
                        System.out.println("   [KSP] Lot zakończony. Analiza wyników...");

                        double score = rewardCalc.calculateReward(
                                result.maxAlt(), result.apoapsis(), result.periapsis(), result.finalFuel(), result.isCrashed()
                        );

                        Files.deleteIfExists(resFile);
                        Files.deleteIfExists(Path.of(telemetryPath));
                        Files.deleteIfExists(Path.of(commandPath));

                        System.out.println("   Czekam na przeładowanie fizyki w KSP...");
                        Thread.sleep(5000);

                        return score;
                    }
                }

                Thread.sleep(200);

            }
        }
    }

    private void logEvolutionStats(int generation, double bestScore) {
        Path statsPath = Path.of("evolution_stats.csv");
        try {
            if (!Files.exists(statsPath)) {
                Files.writeString(statsPath, "Generacja,Najlepszy_Wynik\n", StandardOpenOption.CREATE);
            }
            String line = generation + "," + String.format(java.util.Locale.US, "%.2f", bestScore) + "\n";
            Files.writeString(statsPath, line, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Błąd zapisu statystyk: " + e.getMessage());
        }
    }
}
