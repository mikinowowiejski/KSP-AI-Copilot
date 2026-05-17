package space.copilot.telemetry;

public class RewardCalculator {

    public double calculateReward(double maxAlt, double apo, double peri, double finalFuel, boolean crashed, double maxAoA) {

        double reward = 0.0;

        // 1. NAGRODA ZA WYSOKOŚĆ (Rzeczywiście nieliniowa/pierwiastkowa)

        double effectiveAlt = Math.min(maxAlt, 80000.0);
        reward += (effectiveAlt / 50.0);

        // 2. OSIĄGNIĘCIE PRZESTRZENI (Gradient zamiast ściany)

        if (apo > 0) {
            double cappedApo = Math.min(apo, 70000.0);
            reward += (cappedApo / 35.0);

            if(apo > 85000)
            {
                double overshoot = apo - 85000;
                reward-= (overshoot*0.05);
            }
        }

        // 3. NAGRODA ZA ORBITĘ (Peryapsis gradient)

        if (peri > -620000) {
            double cappedPeri = Math.min(peri, 70000.0);
            reward += ((cappedPeri + 620000) / 100.0);
        }

        // 4. NAGRODA ZA STABILNĄ ORBITĘ (Pełny sukces)
        if (peri >= 70000) {
            reward += 10000.0;
            reward += (finalFuel * 5.0);
        }

        // 5. KARA ZA NIESTABILNOŚĆ (Max AoA zamiast Average)
        if (maxAoA > 5.0) {
            double excessAoA = maxAoA - 5.0;

            double penalty = Math.min(Math.pow(excessAoA, 2) * 5.0, 2000.0);
            reward -= penalty;
        }

        // 6. NAGRODA ZA KOŁOWOŚĆ ORBITY (Circularization)

        if (peri > 0 && apo > 50000) {
            double eccentricityPenalty = Math.abs(apo - peri);
            reward -= (eccentricityPenalty * 0.05);
        }

        // 7. KARA ZA KATASTROFĘ (Aplikowana na końcu)

        if (crashed) {
            reward -= 5000.0;
        }

        return reward;
    }
}