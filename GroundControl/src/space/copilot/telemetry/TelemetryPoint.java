package space.copilot.telemetry;

public record TelemetryPoint(double missionTime, double altitude, double velocity, double pitch) {

    @Override
    public String toString()
    {
        return String.format("[T+ %05.2fs] WYS: %08.1fm | PRĘDKOŚĆ: %06.1fm/s | POCHYLENIE: %04.1f°",
                missionTime, altitude, velocity, pitch);
    }
}
