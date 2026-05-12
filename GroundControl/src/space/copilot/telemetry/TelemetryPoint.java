package space.copilot.telemetry;

public record TelemetryPoint(double missionTime, double altitude, double velocity, double twr, double q, double apoapsis, double pitch) {

    @Override
    public String toString()
    {
        return String.format("[T+ %05.2fs] WYS: %08.1fm | PRĘDKOŚĆ: %06.1fm/s | | TWR: %06.1fN | CIŚNIENIE: %06.1fatm | Apoapsis: %08.1fm | POCHYLENIE: %04.1f°",
                missionTime, altitude, velocity, apoapsis, pitch);
    }
}
