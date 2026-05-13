package space.copilot.telemetry;

public record TelemetryPoint(double missionTime, double altitude, double velocity, double twr, double q, double apoapsis, double etaApo, double pitch) {

    @Override
    public String toString() {
        return String.format(java.util.Locale.US,
                "[T+ %05.1fs] WYS: %05.0fm | SPD: %04.0fm/s | APO: %05.0fm (ETA: %03.0fs) | AI PITCH: %02.1f°",
                missionTime(), altitude(), velocity(), apoapsis(), etaApo(), pitch());
    }
}
