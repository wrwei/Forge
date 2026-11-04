package steam_boiler.sensor;

import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;

/**
 * The quantities the controller reads each cycle. They are written by
 * {@link CycleAnalyser} at the end of the analysis stage, before the
 * controller steps; between cycles each keeps its last value.
 */
public final class BoilerSensor {

    @RoboChartType("nat") int stopCount;
    boolean transmissionFailed;
    boolean waitingReceived;
    boolean unitsReadyReceived;
    @RoboChartType("real") double level;
    @RoboChartType("real") double steam;
    @RoboChartType("real") double throughput;
    @RoboChartType("real") double estimateLow;
    @RoboChartType("real") double estimateHigh = BoilerConstants.CAPACITY_C;
    @RoboChartType("real") double worstLow;
    @RoboChartType("real") double worstHigh = BoilerConstants.CAPACITY_C;
    boolean levelFailed;
    boolean steamFailed;
    boolean anyPumpFailed;
    boolean anyPumpControlFailed;

    /** Number of successive transmissions, up to this one, that carried STOP (capped at 3). */
    @RoboChartType("nat")
    public int consecutiveStopCount() {
        return stopCount;
    }

    /** Whether this transmission lacked an indispensable message or carried an aberrant one. */
    public boolean transmissionFailure() {
        return transmissionFailed;
    }

    /** Whether this transmission carried STEAM_BOILER_WAITING. */
    public boolean steamBoilerWaitingReceived() {
        return waitingReceived;
    }

    /** Whether this transmission carried PHYSICAL_UNITS_READY. */
    public boolean physicalUnitsReadyReceived() {
        return unitsReadyReceived;
    }

    /** q: the water level most recently reported (litre). */
    @RoboChartType("real")
    public double waterLevel() {
        return level;
    }

    /** v: the steam rate most recently reported (litre/sec). */
    @RoboChartType("real")
    public double steamRate() {
        return steam;
    }

    /** p: the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return throughput;
    }

    /** Lower bound on the true water level (litre); equals q while the level device is sound. */
    @RoboChartType("real")
    public double estimatedLevelLow() {
        return estimateLow;
    }

    /** Upper bound on the true water level (litre); equals q while the level device is sound. */
    @RoboChartType("real")
    public double estimatedLevelHigh() {
        return estimateHigh;
    }

    /** Lowest level reachable by the next cycle: no water supplied, steam rising at its maximum. */
    @RoboChartType("real")
    public double worstCaseLevelLow() {
        return worstLow;
    }

    /** Highest level reachable by the next cycle: running pumps keep supplying, steam falling at its maximum. */
    @RoboChartType("real")
    public double worstCaseLevelHigh() {
        return worstHigh;
    }

    /** Whether the water-level measuring device is believed out of order. */
    public boolean levelDeviceFailed() {
        return levelFailed;
    }

    /** Whether the steam measuring device is believed out of order. */
    public boolean steamDeviceFailed() {
        return steamFailed;
    }

    /** Whether some pump is believed out of order. */
    public boolean pumpFailed() {
        return anyPumpFailed;
    }

    /** Whether some pump controller is believed out of order. */
    public boolean pumpControlFailed() {
        return anyPumpControlFailed;
    }
}
