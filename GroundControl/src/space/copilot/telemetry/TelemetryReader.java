package space.copilot.telemetry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TelemetryReader {

    /**
     * odczytuje logi z lotu rakiety zapisane w pliku CSV
     * Wykorzystuje API Streams do przetwarzania danych "w locie".
     */

    public List<TelemetryPoint> readLog(String path) {
        Path filePath = Path.of(path);

        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {

            return new ArrayList<>();
        }

        if (lines.isEmpty()) {
            return new ArrayList<>();
        }

        if (lines.get(0).contains("Czas_s")) {
            lines.remove(0);
        }

        List<TelemetryPoint> data = new ArrayList<>();
        for (String line : lines) {
            try {
                data.add(parseLine(line));
            } catch (Exception e) {
            }
        }

        return data;
    }

    private TelemetryPoint parseLine(String rawLine)
    {
        String[] parts = rawLine.split(",");

        /*if(parts.length != )
        {
            throw new IllegalArgumentException("Uszkodzona ramka telemetryczna!");
        }*/

        double time = Double.parseDouble(parts[0]);
        double altitude = Double.parseDouble(parts[1]);
        double velocity = Double.parseDouble(parts[2]);
        double twr = Double.parseDouble(parts[3]);
        double q = Double.parseDouble(parts[4]);
        double apoapsis = Double.parseDouble(parts[5]);
        double etaApo = Double.parseDouble(parts[6]);
        double pitch = Double.parseDouble(parts[7]);

        return new TelemetryPoint(time,altitude,velocity,twr, q, apoapsis,etaApo, pitch);
    }
}
