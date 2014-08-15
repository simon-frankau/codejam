package name.arbitrary.y2014.qualification;

import name.arbitrary.CodeJamBase;

public class B_CookieClickerAlpha extends CodeJamBase {
    public B_CookieClickerAlpha(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new B_CookieClickerAlpha(args[0]).run();
    }

    @Override
    protected String runCase() {
        String parts[] = getLine().split(" ");
        double c = Double.parseDouble(parts[0]);
        double f = Double.parseDouble(parts[1]);
        double x = Double.parseDouble(parts[2]);

        // Breakeven time. Not worth buying a cookie farm if we have it for less than this time.
        double breakeven = c / f;

        // Most efficient solution is to buy farms as soon as you can.
        // Could do something clever, but... we don't. We calculate each explicitly.
        int nFarms = 0;
        double time = 0;
        // Only buy the next farm if it will pay for itself.
        while (breakeven * ((nFarms+1) * f + 2.0) < x) { // (nFarms < 70) { //(breakeven * (nFarms * f + 2.0) < x) {
            // System.err.println(time + x / (nFarms * f + 2.0));
            double productionRate = nFarms * f + 2.0;
            double timeToNextFarm = c / productionRate;
            time += timeToNextFarm;
            nFarms++;
        }

        // And now, with all those farms, how long will it take to produce x?
        time += x / (nFarms * f + 2.0);

        return String.format("%.7f", time);
    }
}
