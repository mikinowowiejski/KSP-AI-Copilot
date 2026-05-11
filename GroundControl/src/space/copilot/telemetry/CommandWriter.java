package space.copilot.telemetry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class CommandWriter {

    private double lastWrittenPitch = -999.0;

    public void writeCommand(double currentAltitude, String outputPath) throws IOException {
        double targetPitch = calculateGravityTurn(currentAltitude);

        if (Math.abs(targetPitch - lastWrittenPitch) < 0.1) {
            return;
        }

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

            lastWrittenPitch = targetPitch;
        } catch (IOException e) {
        }
    }

    private double calculateGravityTurn(double altitude) {
        if (altitude < 1000) return 90.0;
        if (altitude > 40000) return 0.0;

        double progress = (altitude - 1000) / 39000.0;

        double shapeFactor = 1.7;
        double curvedProgress = Math.pow(progress,shapeFactor);

        return 90 - (curvedProgress*90);
    }


}
