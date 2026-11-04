package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

/**
 * What the program knows on each cycle, derived from the cycle's incoming
 * transmission by {@link PlantMonitor}, which writes it before the controller
 * is stepped. The controller's guards read the boolean verdicts; the
 * measured quantities are kept for inspection.
 */
public final class BoilerSensor {

    boolean shutdownDemanded;
    boolean plantWaiting;
    boolean unitsReady;
    boolean startUpFault;
    boolean levelBelowNormal;
    boolean levelAboveNormal;
    boolean levelAtRisk;
    boolean levelFailure;
    boolean unitFailure;
    boolean estimateUnreliable;
    @RoboChartType("nat") int stopCount;
    @RoboChartType("real") double levelReading;
    @RoboChartType("real") double steamReading;
    @RoboChartType("real") double pumpThroughput;
    @RoboChartType("real") double estimatedLevel;

    /**
     * True when STOP has arrived on three successive transmissions, or when
     * this transmission lacks an indispensable message or carries an
     * aberrant one.
     */
    public boolean shutdownDemanded() { return shutdownDemanded; }

    /** True once STEAM_BOILER_WAITING has arrived, in this or an earlier transmission. */
    public boolean plantWaiting() { return plantWaiting; }

    /** True when this transmission carries PHYSICAL_UNITS_READY. */
    public boolean unitsReady() { return unitsReady; }

    /** True when the reported steam rate is not zero or the level measuring device is out of order. */
    public boolean startUpFault() { return startUpFault; }

    /** True when the best known water level (reading, or estimate while the device is out of order) is below N1. */
    public boolean levelBelowNormal() { return levelBelowNormal; }

    /** True when the best known water level is above N2. */
    public boolean levelAboveNormal() { return levelAboveNormal; }

    /**
     * True when, under worst-case steam and pump behaviour and with the pumps
     * regulated this cycle, the level could reach M1 or M2 before the next cycle.
     */
    public boolean levelAtRisk() { return levelAtRisk; }

    /** True while the level measuring device is out of order. */
    public boolean levelFailure() { return levelFailure; }

    /** True while the steam measuring device, a pump or a pump controller is out of order. */
    public boolean unitFailure() { return unitFailure; }

    /** True while the steam measuring device or a pump controller is out of order, so no level estimate can be trusted. */
    public boolean estimateUnreliable() { return estimateUnreliable; }

    /** Number of successive transmissions, up to this one, that carried STOP. */
    @RoboChartType("nat") public int stopCount() { return stopCount; }

    /** q: the water level reported in this transmission (litre). */
    @RoboChartType("real") public double levelReading() { return levelReading; }

    /** v: the steam rate reported in this transmission (litre/sec). */
    @RoboChartType("real") public double steamReading() { return steamReading; }

    /** p: the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real") public double pumpThroughput() { return pumpThroughput; }

    /** Midpoint of the current lower and upper bounds on the water level (litre). */
    @RoboChartType("real") public double estimatedLevel() { return estimatedLevel; }
}
