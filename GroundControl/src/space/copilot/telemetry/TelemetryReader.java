package space.copilot.telemetry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TelemetryReader {

    /**
     * odczytuje logi z lotu rakiety zapisane w pliku CSV
     * Wykorzystuje API Streams do przetwarzania danych "w locie".
     */

    public List<TelemetryPoint> readLog(String absoluteFilePath) throws IOException
    {
        return Files.lines(Path.of(absoluteFilePath))
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::parseLine)
                .toList();
    }

    private TelemetryPoint parseLine(String rawLine)
    {
        String[] parts = rawLine.split(",");

        if(parts.length != 4)
        {
            throw new IllegalArgumentException("Uszkodzona ramka telemetryczna!");
        }

        double time = Double.parseDouble(parts[0]);
        double altitude = Double.parseDouble(parts[1]);
        double velocity = Double.parseDouble(parts[2]);
        double pitch = Double.parseDouble(parts[3]);

        return new TelemetryPoint(time,altitude,velocity,pitch);
    }
}
