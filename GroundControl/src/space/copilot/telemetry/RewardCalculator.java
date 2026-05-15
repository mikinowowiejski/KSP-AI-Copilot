package space.copilot.telemetry;

public class RewardCalculator {

    public double calculateReward(double maxAlt, double apo, double peri, double finalFuel, boolean crashed, double avgAoA) {

        if (crashed) {
            return -15000.0;
        }

        double reward = 0.0;

        // 2. NAGRODA ZA WYSOKOŚĆ (Logarytmiczna)
        reward += (maxAlt / 50.0);

        // 3. OSIĄGNIĘCIE PRZESTRZENI (Apoapsis > 70km)
        if (apo > 70000) {
            reward += 2000.0;
        }

        // 4. NAGRODA ZA ORBITĘ (Peryapsis)
        if (peri > -50000) {
            reward += (peri + 50000) / 10.0;
        }

        // NAGRODA ZA STABILNĄ ORBITĘ (Pełny sukces)
        if (peri >= 70000) {
            reward += 10000.0;
            reward += (finalFuel * 5.0);
        }

        // 5. KARA ZA NIESTABILNOŚĆ (AoA - Angle of Attack)
        if (avgAoA > 5.0) {
            reward -= (avgAoA * 100.0);
        }

        // 6. NAGRODA ZA KOŁOWOŚĆ ORBITY (Circularization)
        if (peri > 0) {
            double eccentricityPenalty = Math.abs(apo - peri);
            reward -= (eccentricityPenalty * 0.05);
        }

        return reward;
    }
}