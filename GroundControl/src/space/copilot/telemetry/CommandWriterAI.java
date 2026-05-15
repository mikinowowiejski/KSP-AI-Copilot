package space.copilot.telemetry;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CommandWriterAI {

    private MultiLayerNetwork aiModel;
    private double lastWrittenPitch = -999.0;

    public void loadAI(String modelPath) throws IOException {
        System.out.println("Ładowanie modułu AI z pliku: " + modelPath);
        this.aiModel = MultiLayerNetwork.load(new File(modelPath), true);
        System.out.println("====== MÓZG AI ZAŁADOWANY  ======");
    }

    public void writeCommand(double altitude, double spd, double twr, double q, double apo, double etaApo, String outputPath) throws IOException {

        double nAlt = altitude / 70000.0;
        double nSpd = spd / 2500.0;
        double nTwr = twr / 10.0;
        double nQ = q / 0.5;
        double nApo = apo / 80000;
        double nEta = etaApo / 100;

        if (aiModel == null) {
            throw new IllegalStateException("Model AI nie został załadowany! Wywołaj loadAI() przed lotem.");
        }

        INDArray input = Nd4j.create(new double[]{ nAlt, nSpd, nTwr, nQ, nApo, nEta }, new int[]{1, 6});

        INDArray output = aiModel.output(input);

        double predictedPitch = output.getDouble(0) * 90.0;
        if (predictedPitch < 0.0) predictedPitch = 0.0;
        if (predictedPitch > 90.0) predictedPitch = 90.0;

        double predictedThrottle = output.getDouble(1);
        if (predictedThrottle < 0.0) predictedThrottle = 0.0;
        if (predictedThrottle > 1.0) predictedThrottle = 1.0;

        double rawStaging = output.getDouble(2);
        int stagingFlag = (rawStaging > 0.4) ? 1 : 0;

        writeToCsvWithLock(predictedPitch, predictedThrottle, stagingFlag, outputPath);
    }


    private void writeToCsvWithLock(double pitch, double throttle, int staging, String outputPath) throws IOException
    {
        Path dataPath = Path.of(outputPath);

        Path flagPath = dataPath.getParent().resolve("ksp_ready.flag");

        if (Files.exists(flagPath)) {

            String command = String.format(java.util.Locale.US, "%.2f,%.2f,%d", pitch, throttle, staging);

            try {
                Files.writeString(dataPath, command, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                Files.deleteIfExists(flagPath);

                lastWrittenPitch = pitch;
            } catch (java.io.IOException e) {

            }
        }
    }

    public void setAiModel(MultiLayerNetwork model) {
        this.aiModel = model;
    }
}