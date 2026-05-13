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

        double normalizedAltitude = altitude / 50000.0;

        INDArray input = Nd4j.create(new double[]{ nAlt, nSpd, nTwr, nQ, nApo, nEta }, new int[]{1, 6});

        INDArray output = aiModel.output(input);

        double predictedPitch = output.getDouble(0) * 90.0;

        if (predictedPitch < 0.0) predictedPitch = 0.0;
        if (predictedPitch > 90.0) predictedPitch = 90.0;

        writeToCsvWithLock(predictedPitch, outputPath);

    }

    private void writeToCsvWithLock(double pitch, String outputPath) throws IOException
    {
        if (Math.abs(pitch - lastWrittenPitch) < 0.1) return;

        Path finalPath = Path.of(outputPath);
        Path lockPath = Path.of(outputPath + ".lock");
        String command = String.format(Locale.US, "%.2f", pitch);

        try {
            Files.writeString(lockPath, "LOCKED");
            Files.writeString(finalPath, command, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            lastWrittenPitch = pitch;
        } finally {
            Files.deleteIfExists(lockPath);
        }
    }


    public void setAiModel(MultiLayerNetwork model) {
        this.aiModel = model;
    }
}