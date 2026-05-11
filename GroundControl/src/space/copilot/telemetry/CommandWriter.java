package space.copilot.telemetry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class CommandWriter {
    public void writeCommand(double currentAltitude, String outputPath) throws IOException {
        double targetPitch = calculateGravityTurn(currentAltitude);
        String command = String.format(Locale.US, "%.2f", targetPitch);

        Path finalPath = Path.of(outputPath);
        Path tempPath = Path.of(outputPath + ".tmp"); // Plik tymczasowy

        Files.writeString(tempPath, command,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        try {
            Files.move(tempPath, finalPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            System.out.println(String.format("Ground Control: Wysokość %.0fm -> Kąt: %s°",
                    currentAltitude, command));
        } catch (IOException e) {
        }
    }

    private double calculateGravityTurn(double altitude) {
        if (altitude < 1000) return 90.0;
        if (altitude > 40000) return 0.0;
        double progress = (altitude - 1000) / 39000.0;
        return 90.0 - (progress * 90.0);
    }


}
