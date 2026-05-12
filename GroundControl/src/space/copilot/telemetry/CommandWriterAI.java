package space.copilot.telemetry;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class CommandWriterAI {

    private MultiLayerNetwork aiModel;
    private double lastWrittenPitch = -999.0;

    public void loadAI(String modelPath) throws IOException {
        System.out.println("Ładowanie modułu AI z pliku: " + modelPath);
        this.aiModel = MultiLayerNetwork.load(new File(modelPath), true);
        System.out.println("====== MÓZG AI ZAŁADOWANY  ======");
    }

    public void writeCommand(double altitude, double spd, double twr, double q, double apo, String outputPath) throws IOException {

        double nAlt = altitude / 70000.0;
        double nSpd = spd / 2500.0;
        double nTwr = twr / 10.0;
        double nQ = q / 0.5;
        double nApo = apo / 80000;

        if (aiModel == null) {
            throw new IllegalStateException("Model AI nie został załadowany! Wywołaj loadAI() przed lotem.");
        }

        double normalizedAltitude = altitude / 50000.0;

        INDArray input = Nd4j.create(new double[]{ nAlt, nSpd, nTwr, nQ, nApo }, new int[]{1, 5});

        INDArray output = aiModel.output(input);

        double predictedPitch = output.getDouble(0) * 90.0;

        if (predictedPitch < 0.0) predictedPitch = 0.0;
        if (predictedPitch > 90.0) predictedPitch = 90.0;

        if (Math.abs(predictedPitch - lastWrittenPitch) < 0.1) {
            return;
        }

        String command = String.format(Locale.US, "%.2f", predictedPitch);

        Path finalPath = Path.of(outputPath);
        Path tempPath = Path.of(outputPath + ".tmp");

        Files.writeString(tempPath, command,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        try {
            Files.move(tempPath, finalPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            System.out.println(String.format("AI Copilot: Wysokość %.0fm -> Wychylenie: %.2f°",
                    altitude, predictedPitch));

            lastWrittenPitch = predictedPitch;

        } catch (IOException e) {
        }
    }
}