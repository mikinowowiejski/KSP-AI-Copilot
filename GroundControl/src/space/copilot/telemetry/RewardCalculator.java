package space.copilot.telemetry;

public class RewardCalculator {

    public double calculateReward(double maxAlt, double apo, double peri, double finalFuel, boolean crashed)
    {
        if(crashed)
        {
            return -10000.0;
        }

        double reward = 0.0;

        reward+=(maxAlt/100.0);

        if(apo > 70000)
        {
            reward+= 1000.0;
        }

        if(peri > -60000)
        {
            reward += ((peri + 600000) / 500.0);
        }

        if(peri > 0)
        {
            reward += (peri / 20.0);
        }

        if(peri >= 70000)
        {
            reward+= 5000.0;
            reward+=(finalFuel*3.0);

        }

        if (apo > 0 && peri > -600000) {
            double difference = Math.abs(apo - peri);
            reward -= (difference * 0.01);
        }

        return reward;
    }
}
