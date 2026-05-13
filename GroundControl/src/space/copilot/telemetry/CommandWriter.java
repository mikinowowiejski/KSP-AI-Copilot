package space.copilot.telemetry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class CommandWriter {

    private double lastWrittenPitch = -999.0;

    public void writeCommand(double currentAltitude, double apoapsis, String outputPath) throws IOException {
        double targetPitch = calculateGravityTurn(currentAltitude, apoapsis);

        if (Math.abs(targetPitch - lastWrittenPitch) < 0.1) {
            return;
        }

        String command = String.format(Locale.US, "%.2f", targetPitch);

        Path finalPath = Path.of(outputPath);
        Path lockPath = Path.of(outputPath + ".lock");

        try {
            Files.writeString(lockPath, "LOCKED");

            Files.writeString(finalPath, command,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println(String.format("Ground Control: Wysokość %.0fm -> Kąt: %s°",
                    currentAltitude, command));

            lastWrittenPitch = targetPitch;

        } catch (IOException e) {
        } finally {
            try {
                Files.deleteIfExists(lockPath);
            } catch (IOException e) {}
        }
    }

    private double calculateGravityTurn(double altitude, double apoapsis) {
        if (apoapsis >= 80000) {
            return 0.0;
        }

        if (altitude < 1000) {
            return 90.0;
        }

        if (altitude <= 15000) {
            double progress = (altitude - 1000) / 14000.0;
            return 90.0 - (progress * 45.0);
        }

        if (altitude <= 50000) {
            double progress = (altitude - 15000) / 35000.0;
            return 45.0 - (progress * 45.0);
        }

        return 0.0;
    }


}
