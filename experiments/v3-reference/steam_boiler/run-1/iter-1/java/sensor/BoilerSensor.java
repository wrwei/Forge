package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

/**
 * The quantities the controller reads on each cycle. They are derived from
 * the cycle's incoming transmission by {@link PlantMonitor}, which writes
 * them before the controller is stepped.
 */
public final class BoilerSensor {

    @RoboChartType("nat") int stopCount;
    boolean transmissionFailed;
    boolean waitingReceived;
    boolean unitsReadyReceived;
    @RoboChartType("real") double levelReading;
    @RoboChartType("real") double steamReading;
    @RoboChartType("real") double pumpThroughput;
    boolean levelFailure;
    boolean steamFailure;
    boolean pumpFailure;
    boolean pumpControlFailure;
    @RoboChartType("real") double estimatedLevel;
    @RoboChartType("real") double predictedMinimum;
    @RoboChartType("real") double predictedMaximum;
    @RoboChartType("real") double predictedMaximumWithoutPumps;

    /** Number of successive transmissions, up to this one, that carried STOP. */
    @RoboChartType("nat") public int stopCount() { return stopCount; }

    /** True when this transmission lacks an indispensable message or carries an aberrant one. */
    public boolean transmissionFailed() { return transmissionFailed; }

    /** True when this transmission carries STEAM_BOILER_WAITING. */
    public boolean waitingReceived() { return waitingReceived; }

    /** True when this transmission carries PHYSICAL_UNITS_READY. */
    public boolean unitsReadyReceived() { return unitsReadyReceived; }

    /** q: the water level reported in this transmission (litre). */
    @RoboChartType("real") public double levelReading() { return levelReading; }

    /** v: the steam rate reported in this transmission (litre/sec). */
    @RoboChartType("real") public double steamReading() { return steamReading; }

    /** p: the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real") public double pumpThroughput() { return pumpThroughput; }

    /** True while the level measuring device is out of order. */
    public boolean levelFailure() { return levelFailure; }

    /** True while the steam measuring device is out of order. */
    public boolean steamFailure() { return steamFailure; }

    /** True while some pump is out of order. */
    public boolean pumpFailure() { return pumpFailure; }

    /** True while some pump controller is out of order. */
    public boolean pumpControlFailure() { return pumpControlFailure; }

    /** Midpoint of the current lower and upper bounds on the water level (litre). */
    @RoboChartType("real") public double estimatedLevel() { return estimatedLevel; }

    /** Lowest level reachable by the next cycle in the worst case (litre). */
    @RoboChartType("real") public double predictedMinimum() { return predictedMinimum; }

    /** Highest level reachable by the next cycle if the running pumps stay on (litre). */
    @RoboChartType("real") public double predictedMaximum() { return predictedMaximum; }

    /** Highest level reachable by the next cycle if every pump is closed now (litre). */
    @RoboChartType("real") public double predictedMaximumWithoutPumps() { return predictedMaximumWithoutPumps; }
}
