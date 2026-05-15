package space.copilot.telemetry;

import krpc.client.Connection;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class EvolutionTrainer {

    private static final double NOISE_SCALE = 0.05;
    private static final double MUTATION_RATE = 0.02;
    double absoluteBestScore = -Double.MAX_VALUE;

    private final RewardCalculator rewardCalc = new RewardCalculator();

    public MultiLayerNetwork loadBaseModel(String path) throws IOException {
        return MultiLayerNetwork.load(new File(path), true);
    }

    public List<MultiLayerNetwork> createMutants(MultiLayerNetwork parent, int numberOfChildren) {
        List<MultiLayerNetwork> children = new ArrayList<>();
        for(int i = 0; i < numberOfChildren; i++) {
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

    public void runEvolution(String baseModelPath, int generations, int childrenPerGen) throws Exception {
        MultiLayerNetwork currentBestModel;
        File modelFile = new File("absolute_master.zip");

        if (modelFile.exists()) {
            System.out.println(">>> Znaleziono zapisanego Mistrza. Ładuję model i rekord...");
            currentBestModel = ModelSerializer.restoreMultiLayerNetwork(modelFile);
            this.absoluteBestScore = loadBestScore();
            System.out.println(">>> Rekord do pobicia: " + absoluteBestScore);
        } else {
            System.out.println(">>> Brak zapisanego modelu. Tworzę Adama 2.0 od zera...");
            currentBestModel = createBlankModel();
            this.absoluteBestScore = -Double.MAX_VALUE;
        }

        for(int gen = 1; gen <= generations; gen++) {
            System.out.println("\n--- GENERACJA " + gen + "---");
            List<MultiLayerNetwork> children = createMutants(currentBestModel, childrenPerGen-1);
            children.add(0, currentBestModel.clone());

            MultiLayerNetwork bestChild = null;
            double bestScore = -999999.0;

            for(int i = 0; i < children.size(); i++) {
                if (i == 0) {
                    System.out.println(">> Lot Mistrza (Rodzica)... ");
                } else {
                    System.out.println(">> Lot mutanta nr " + i + "/" + (childrenPerGen - 1));
                }

                double score = performFlight(children.get(i));

                System.out.println("Wynik: " + score + " pkt.");

                if (score > bestScore) {
                    bestScore = score;
                    bestChild = children.get(i);
                }
            }

            System.out.println("\nZwycięzca Generacji " + gen + " zdobył: " + bestScore + " pkt.");

            if (bestChild == children.get(0)) {
                System.out.println(">>> STARY MISTRZ OBRONIŁ TYTUŁ! (Nadal jest najlepszy)");
            } else {
                System.out.println(">>> MAMY NOWEGO MISTRZA! (Mutant pokonał rodzica)");
            }


            currentBestModel = bestChild;
            ModelSerializer.writeModel(currentBestModel, "absolute_master.zip", true);


            if (bestScore > absoluteBestScore) {
                absoluteBestScore = bestScore;
                saveBestScore(absoluteBestScore);
            }

            logEvolutionStats(gen, absoluteBestScore);
            System.gc();
        }
    }



    private double performFlight(MultiLayerNetwork aiModel) {

        /*
            ===========================================================================
                                        POLACZENIE Z KSP
            ===========================================================================

         */
        try (Connection connection = Connection.newInstance("AI Pilot")) {
            SpaceCenter ksp = SpaceCenter.newInstance(connection);
            Vessel vessel = ksp.getActiveVessel();
            AutoPilot autoPilot = vessel.getAutoPilot();
            Flight flight = vessel.flight(vessel.getSurfaceReferenceFrame());

            System.out.println("   [KSP] Przejmowanie sterów...");

            autoPilot.engage();
            autoPilot.targetPitchAndHeading(90f, 90f);
            vessel.getControl().setThrottle(1.0f);

            System.out.println("   [KSP] 3... 2... 1... START!");
            vessel.getControl().activateNextStage();

            boolean isCrashed = false;
            double maxAlt = 0;
            double startTime = ksp.getUT();
            double lastStageTime = startTime + 3.0;
            double emptyStageTimer = 0;
            double totalAoA = 0;
            int ticks = 0;

            //GLOWNA PETLA - az nie osiagniemy orbity
            while (vessel.getOrbit().getPeriapsisAltitude() < 75000 && !isCrashed) {

                /*
                   ===========================================================================
                                                POBRANIE DANYCH
                   ===========================================================================

                 */

                double altitude = flight.getSurfaceAltitude();
                double speed = flight.getSpeed();
                double apoapsis = vessel.getOrbit().getApoapsisAltitude();
                double periapsis = vessel.getOrbit().getPeriapsisAltitude();
                double q = flight.getDynamicPressure() / 1000.0;
                double maxThrust = vessel.getAvailableThrust();
                double mass = vessel.getMass();
                double twr = (mass > 0) ? (maxThrust / (mass * 9.81)) : 0;
                double etaApo = vessel.getOrbit().getTimeToApoapsis();
                double actualPitch = flight.getPitch();

                double aoa = flight.getAngleOfAttack();
                totalAoA+= Math.abs(aoa);
                ticks++;

                double isp = vessel.getSpecificImpulse();
                double termVel = flight.getTerminalVelocity();
                if (Double.isInfinite(termVel) || termVel <= 0) termVel = 9999.0;
                double gForce = flight.getGForce();

                if (altitude > maxAlt) maxAlt = altitude;

                double totalFuel = vessel.getResources().amount("LiquidFuel") + vessel.getResources().amount("SolidFuel");

                /*
                ===========================================================================
                                            WARUNKI KATASTROF
                ===========================================================================
                 */

                if (totalFuel < 0.1 && periapsis < 70000) {
                    System.out.println("   [KATASTROFA] Brak paliwa!"); isCrashed = true;
                } else if (flight.getVerticalSpeed() < -5 && altitude > 1000 && periapsis < 70000) {
                    System.out.println("   [KATASTROFA] Rakieta spada!"); isCrashed = true;
                } else if (altitude < 50000 && actualPitch < 0) {
                    System.out.println("   [KATASTROFA] Nos opadł w atmosferze!"); isCrashed = true;
                } else if (apoapsis > 120000 && periapsis < 20000) {
                    System.out.println("   [KATASTROFA] Lot zbyt pionowy (Strzała)!"); isCrashed = true;
                } else if (ksp.getUT() - startTime > 540 && periapsis < 70000) {
                    System.out.println("   [KATASTROFA] Limit czasu (9 minut)!"); isCrashed = true;
                }

                if (maxThrust == 0 && periapsis < 70000) {
                    emptyStageTimer += 0.2;
                    if (emptyStageTimer > 5.0) {
                        System.out.println("   [KATASTROFA] Martwy ciąg, AI unika stagingu!"); isCrashed = true;
                    }
                } else {
                    emptyStageTimer = 0;
                }

                try {
                    if (vessel.getParts().getRoot() == null) {
                        System.out.println("   [KATASTROFA] RAKIETA WYBUCHŁA!");
                        isCrashed = true;
                        break;
                    }
                } catch (Exception e) {
                    isCrashed = true;
                    break;
                }

                if (isCrashed) break;

                /*
                ===========================================================================
                            WNIOSKOWANIE AI (Mózg przejmuje kontrolę)
                ===========================================================================
                 */

                double nAlt = altitude / 70000.0;
                double nSpd = speed / 2500.0;
                double nTwr = twr / 10.0;
                double nQ = q / 0.5;
                double nApo = apoapsis / 80000.0;
                double nEta = etaApo / 100.0;

                double nAoA = aoa / 20.0;
                double nIsp = isp / 400.0;
                double nG = gForce / 10.0;
                double nTerm = speed / termVel;
                if (nTerm > 2.0) nTerm = 2.0;




                INDArray input = Nd4j.create(new double[]{
                        nAlt, nSpd, nTwr, nQ, nApo, nEta, nAoA, nIsp, nTerm, nG
                }, new int[]{1, 10});
                INDArray output = aiModel.output(input);

                float targetPitch = (float) (output.getDouble(0) * 90.0);
                if (targetPitch < 0f) targetPitch = 0f;
                if (targetPitch > 90f) targetPitch = 90f;

                float targetThrottle = (float) output.getDouble(1);
                if (targetThrottle < 0f) targetThrottle = 0f;
                if (targetThrottle > 1f) targetThrottle = 1f;

                boolean wantsToStage = output.getDouble(2) > 0.4;

                /*
                ===========================================================================
                                WYKONANIE KOMEND W KSP (Wirtualny drążek)
                ===========================================================================
                 */


                autoPilot.targetPitchAndHeading(targetPitch, 90f);
                vessel.getControl().setThrottle(targetThrottle);

                if (wantsToStage && ksp.getUT() > lastStageTime + 2.0) {
                    vessel.getControl().activateNextStage();
                    lastStageTime = ksp.getUT();
                    System.out.println("   [AI] Wykonano staging!");
                }

                /*
                 ===========================================================================
                                     WYSWIETLENIE PARAMETROW NA KONSOLI
                ===========================================================================
                 */
                double missionTime = ksp.getUT() - startTime;

                System.out.printf(java.util.Locale.US,
                        "\r   [T+%04.1fs] WYS: %5.0fm | SPD: %4.0fm/s | PITCH: %2.0f° | APO: %5.0fkm | GAZ: %3.0f%%%s          ",
                        missionTime,
                        altitude,
                        speed,
                        targetPitch,
                        apoapsis / 1000.0,
                        targetThrottle * 100,
                        wantsToStage ? " [STAGE!]" : ""
                );
                Thread.sleep(50);
            }

            System.out.println();

            autoPilot.disengage();
            vessel.getControl().setThrottle(0f);

            if (!isCrashed) {
                System.out.println("   [SUKCES] Orbita osiągnięta!");
            }

            double finalFuel = vessel.getResources().amount("LiquidFuel");
            double finalApoapsis = vessel.getOrbit().getApoapsisAltitude();
            double finalPeriapsis = vessel.getOrbit().getPeriapsisAltitude();
            double averageAoA = (ticks > 0) ? (totalAoA / ticks) : 0;

            // Wyliczanie nagrody za ten lot
            double score = rewardCalc.calculateReward(maxAlt, finalApoapsis, finalPeriapsis, finalFuel, isCrashed, averageAoA);

            System.out.println("   Wczytywanie Quicksave'a przez kRPC...");
            ksp.quickload();

            // Dajemy silnikowi Unity w KSP 5 sekund na załadowanie fizyki przed kolejnym lotem
            Thread.sleep(5000);

            return score;

        } catch (Exception e) {
            System.err.println("Błąd podczas lotu kRPC: " + e.getMessage());
            e.printStackTrace();
            return -999999.0;
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

    public MultiLayerNetwork createBlankModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(System.currentTimeMillis()) // Losowość na start
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new DenseLayer.Builder().nIn(10).nOut(64).activation(Activation.TANH).build())
                .layer(new DenseLayer.Builder().nIn(64).nOut(32).activation(Activation.TANH).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.SIGMOID).nIn(32).nOut(3).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    private void saveBestScore(double score) {
        try {
            Files.writeString(Path.of("best_score.txt"), String.valueOf(score));
        } catch (IOException e) {
            System.err.println("Błąd zapisu rekordu: " + e.getMessage());
        }
    }

    private double loadBestScore() {
        try {
            Path path = Path.of("best_score.txt");
            if (Files.exists(path)) {
                return Double.parseDouble(Files.readString(path));
            }
        } catch (Exception e) {
            return -Double.MAX_VALUE;
        }
        return -Double.MAX_VALUE;
    }
}