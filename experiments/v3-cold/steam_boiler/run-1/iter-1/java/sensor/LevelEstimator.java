package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;

// The water level as a lower and an upper bound. While the level device is
// trusted both bounds equal its reading; otherwise they are advanced each cycle
// from the pumps (one litre pumped is one litre gained) and the worst-case
// steam dynamics bounded by W, U1 and U2.
final class LevelEstimator {
    @RoboChartType("real") private static final double C = SteamBoilerConstants.CAPACITY_C;
    @RoboChartType("real") private static final double W = SteamBoilerConstants.MAX_STEAM_W;
    @RoboChartType("real") private static final double U1 = SteamBoilerConstants.STEAM_RISE_U1;
    @RoboChartType("real") private static final double U2 = SteamBoilerConstants.STEAM_FALL_U2;
    @RoboChartType("real") private static final double P = SteamBoilerConstants.PUMP_CAPACITY_P;
    @RoboChartType("real") private static final double DT = SteamBoilerConstants.CYCLE_SECONDS;

    @RoboChartType("real") private double levelLow = 0.0;
    @RoboChartType("real") private double levelHigh = C;
    @RoboChartType("real") private double steamLow = 0.0;
    @RoboChartType("real") private double steamHigh = W;
    @RoboChartType("real") private double admissibleLow = 0.0;
    @RoboChartType("real") private double admissibleHigh = C;
    private boolean initialised = false;

    // The levels compatible with the previous cycle's bounds, the pumps that poured
    // water during the elapsed cycle, and the steam that can have left the boiler.
    void advance(@RoboChartType("nat") int knownPumps, @RoboChartType("nat") int possiblePumps, boolean valveOpen) {
        if (!initialised) {
            admissibleLow = 0.0;
            admissibleHigh = C;
            return;
        }
        double low = levelLow + DT * P * knownPumps - steamOutMax(steamHigh);
        double high = levelHigh + DT * P * possiblePumps - steamOutMin(steamLow);
        if (valveOpen) {
            // The evacuation rate of the valve is not specified.
            low = 0.0;
        }
        admissibleLow = clamp(low);
        admissibleHigh = clamp(high);
    }

    // Within 0..C, and reachable from the previous cycle.
    boolean levelPlausible(@RoboChartType("real") double reading) {
        boolean inRange = reading >= 0.0 && reading <= C;
        if (!initialised) {
            return inRange;
        }
        return inRange && reading >= admissibleLow && reading <= admissibleHigh;
    }

    // Within 0..W, and changed no faster than U1 (increase) and U2 (decrease) allow.
    boolean steamPlausible(@RoboChartType("real") double reading) {
        boolean inRange = reading >= 0.0 && reading <= W;
        if (!initialised) {
            return inRange;
        }
        return inRange && reading >= steamLow - U2 * DT && reading <= steamHigh + U1 * DT;
    }

    void settle(boolean levelTrusted, @RoboChartType("real") double levelReading,
            boolean steamTrusted, @RoboChartType("real") double steamReading) {
        if (levelTrusted) {
            levelLow = levelReading;
            levelHigh = levelReading;
        } else {
            levelLow = admissibleLow;
            levelHigh = admissibleHigh;
        }
        if (steamTrusted) {
            steamLow = steamReading;
            steamHigh = steamReading;
        } else {
            steamLow = 0.0;
            steamHigh = W;
        }
        initialised = true;
    }

    @RoboChartType("real")
    double levelLow() {
        return levelLow;
    }

    @RoboChartType("real")
    double levelHigh() {
        return levelHigh;
    }

    // Lowest level at the end of the next cycle: steam rising at U1, no water assumed from the pumps.
    @RoboChartType("real")
    double predictedMin() {
        return levelLow - steamOutMax(steamHigh);
    }

    // Highest level at the end of the next cycle: every pump that may pour does so at P, steam falling at U2.
    // A pump started in this cycle pours nothing before the next one.
    @RoboChartType("real")
    double predictedMax(@RoboChartType("nat") int possiblePumps) {
        return levelHigh + DT * P * possiblePumps - steamOutMin(steamLow);
    }

    @RoboChartType("real")
    private static double steamOutMax(@RoboChartType("real") double rate) {
        return Math.min(DT * W, DT * rate + U1 * DT * DT / 2.0);
    }

    @RoboChartType("real")
    private static double steamOutMin(@RoboChartType("real") double rate) {
        return Math.max(0.0, DT * rate - U2 * DT * DT / 2.0);
    }

    @RoboChartType("real")
    private static double clamp(@RoboChartType("real") double level) {
        return Math.max(0.0, Math.min(C, level));
    }
}
