package space.copilot.telemetry;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TelemetryTrainer {

    public void trainModel(String trainingDataPath, String savePath) throws IOException {
        System.out.println("ZACZYNAMY TRENING AI...");

        List<String> lines = Files.readAllLines(Paths.get(trainingDataPath)

        );

        if (lines.get(0).contains("Czas_s")) {
            lines.remove(0);
        }

        int numRows = lines.size();
        INDArray input = Nd4j.create(numRows, 6);
        INDArray output = Nd4j.create(numRows, 3);


        double[] smearedStaging = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            String[] cols = lines.get(i).split(",");
            double originalStaging = Double.parseDouble(cols[9]);
            smearedStaging[i] = originalStaging;

            for (int j = Math.max(0, i - 4); j <= Math.min(numRows - 1, i + 4); j++) {
                String[] neighborCols = lines.get(j).split(",");
                if (Double.parseDouble(neighborCols[9]) == 1.0) {
                    smearedStaging[i] = 1.0;
                }
            }
        }

        for (int i = 0; i < numRows; i++) {
            String[] cols = lines.get(i).split(",");
            double altitude = Double.parseDouble(cols[1]);
            double speed = Double.parseDouble(cols[2]);
            double twr = Double.parseDouble(cols[3]);
            double q = Double.parseDouble(cols[4]);
            double apo = Double.parseDouble(cols[5]);
            double etaApo = Double.parseDouble(cols[6]);

            double pitch = Double.parseDouble(cols[7]);
            double throttle = Double.parseDouble(cols[8]);
            double staging = smearedStaging[i];

            double nAlt = altitude / 70000.0;
            double nSpd = speed / 2500.0;
            double nTwr = twr / 10.0;
            double nQ = q / 0.5;
            double nApo = apo / 80000;
            double nEta = etaApo / 100;

            // Normalizacja danych (AI lubi liczby od 0 do 1)
            input.putScalar(new int[]{i, 0}, nAlt);
            input.putScalar(new int[]{i, 1}, nSpd);
            input.putScalar(new int[]{i, 2}, nTwr);
            input.putScalar(new int[]{i, 3}, nQ);
            input.putScalar(new int[]{i, 4}, nApo);
            input.putScalar(new int[]{i, 5}, nEta);

            output.putScalar(new int[]{i, 0}, pitch / 90.0);
            output.putScalar(new int[]{i,1}, throttle);
            output.putScalar(new int[]{i,2}, staging);
        }

        DataSet dataSet = new DataSet(input, output);
        ListDataSetIterator<DataSet> iterator = new ListDataSetIterator<>(dataSet.asList(), 16);

        // 2. Konfiguracja architektury sieci neuronowej
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new DenseLayer.Builder().nIn(6).nOut(32).activation(Activation.TANH).build())
                .layer(new DenseLayer.Builder().nIn(32).nOut(32).activation(Activation.TANH).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.SIGMOID).nIn(32).nOut(3).build())
                .build();

        // 3. Inicjalizacja i trening
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        System.out.println("Uczenie sieci (to może chwilę potrwać)...");
        for (int epoch = 0; epoch < 400; epoch++) {
            iterator.reset();
            model.fit(iterator);

            double score = model.score();

            if (epoch % 10 == 0) {
                System.out.println(String.format("Epoka %d | Błąd (Loss): %.6f", epoch, score));
            }
        }

        // 4. Zapisanie "mózgu" do pliku
        model.save(new File(savePath), true);
        System.out.println("MODEL ZAPISANY DO: " + savePath);
    }
}
