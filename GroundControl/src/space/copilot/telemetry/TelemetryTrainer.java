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

        List<String> lines = Files.readAllLines(Paths.get(trainingDataPath));
        lines.remove(0);

        int numRows = lines.size();
        INDArray input = Nd4j.create(numRows, 1);
        INDArray output = Nd4j.create(numRows, 1);

        for (int i = 0; i < numRows; i++) {
            String[] cols = lines.get(i).split(",");
            double altitude = Double.parseDouble(cols[1]); // Kolumna z wysokością
            double pitch = Double.parseDouble(cols[3]);    // Kolumna z kątem

            // Normalizacja danych (AI lubi liczby od 0 do 1)
            input.putScalar(new int[]{i, 0}, altitude / 50000.0);
            output.putScalar(new int[]{i, 0}, pitch / 90.0);
        }

        DataSet dataSet = new DataSet(input, output);
        ListDataSetIterator<DataSet> iterator = new ListDataSetIterator<>(dataSet.asList(), 16);

        // 2. Konfiguracja architektury sieci neuronowej
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new DenseLayer.Builder().nIn(1).nOut(20).activation(Activation.TANH).build())
                .layer(new DenseLayer.Builder().nIn(20).nOut(20).activation(Activation.TANH).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(20).nOut(1).build())
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
