package space.copilot.telemetry;

public record ResultPoint(
        double maxAlt,
        double apoapsis,
        double periapsis,
        double finalFuel,
        boolean isCrashed)
{}
