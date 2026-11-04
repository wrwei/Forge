package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

/**
 * The program's view of the physical plant for the current cycle: the quantities delivered by the
 * transmission system or derived from them, and the receive stage's judgements about the
 * transmission, the units and the water level. {@link MessageReceiver} fills it at the end of each
 * receive stage; the controller only reads it.
 */
public final class PlantSensor {

    @RoboChartType("real") private double levelReading;
    @RoboChartType("real") private double steamReading;
    @RoboChartType("real") private double pumpThroughput;
    @RoboChartType("real") private double levelEstimateLow;
    @RoboChartType("real") private double levelEstimateHigh;
    @RoboChartType("nat") private int successiveStops;
    private boolean steamBoilerWaitingSeen;
    private boolean physicalUnitsReadyReceived;
    private boolean transmissionFailed;
    private boolean steamRateNonZero;
    private boolean levelDeviceOutOfOrder;
    private boolean otherUnitOutOfOrder;
    private boolean rescueEstimateUnreliable;
    private boolean levelAboveNormal;
    private boolean levelBelowNormal;
    private boolean levelLimitRisk;
    private boolean levelHeadingLow;
    private boolean levelHeadingHigh;

    void recordQuantities(
            @RoboChartType("real") double level,
            @RoboChartType("real") double steam,
            @RoboChartType("real") double throughput,
            @RoboChartType("real") double estimateLow,
            @RoboChartType("real") double estimateHigh) {
        this.levelReading = level;
        this.steamReading = steam;
        this.pumpThroughput = throughput;
        this.levelEstimateLow = estimateLow;
        this.levelEstimateHigh = estimateHigh;
    }

    void recordMessages(@RoboChartType("nat") int stops, boolean waitingSeen, boolean unitsReady, boolean failed) {
        this.successiveStops = stops;
        this.steamBoilerWaitingSeen = waitingSeen;
        this.physicalUnitsReadyReceived = unitsReady;
        this.transmissionFailed = failed;
    }

    void recordUnits(boolean levelDevice, boolean otherUnit, boolean estimateUnreliable) {
        this.levelDeviceOutOfOrder = levelDevice;
        this.otherUnitOutOfOrder = otherUnit;
        this.rescueEstimateUnreliable = estimateUnreliable;
    }

    void recordLevelJudgements(
            boolean steamNonZero,
            boolean aboveNormal,
            boolean belowNormal,
            boolean limitRisk,
            boolean headingLow,
            boolean headingHigh) {
        this.steamRateNonZero = steamNonZero;
        this.levelAboveNormal = aboveNormal;
        this.levelBelowNormal = belowNormal;
        this.levelLimitRisk = limitRisk;
        this.levelHeadingLow = headingLow;
        this.levelHeadingHigh = headingHigh;
    }

    /** q: the water level most recently reported by the level measuring device (litres). */
    @RoboChartType("real")
    public double levelReading() {
        return levelReading;
    }

    /** v: the steam rate most recently reported by the steam measuring device (litres/sec). */
    @RoboChartType("real")
    public double steamReading() {
        return steamReading;
    }

    /** p: the throughput of the pumps reported on whose controllers see water flowing (litres/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return pumpThroughput;
    }

    /** Lower bound of the current water level: q when the level device works, else the estimate. */
    @RoboChartType("real")
    public double levelEstimateLow() {
        return levelEstimateLow;
    }

    /** Upper bound of the current water level: q when the level device works, else the estimate. */
    @RoboChartType("real")
    public double levelEstimateHigh() {
        return levelEstimateHigh;
    }

    /** How many successive transmissions, up to and including this one, carried STOP. */
    @RoboChartType("nat")
    public int successiveStops() {
        return successiveStops;
    }

    /** Whether STEAM_BOILER_WAITING has arrived, in this transmission or an earlier one. */
    public boolean steamBoilerWaitingSeen() {
        return steamBoilerWaitingSeen;
    }

    /** Whether this transmission carried PHYSICAL_UNITS_READY. */
    public boolean physicalUnitsReadyReceived() {
        return physicalUnitsReadyReceived;
    }

    /** Whether this transmission lacked an indispensable message or carried an aberrant one. */
    public boolean transmissionFailed() {
        return transmissionFailed;
    }

    /** Whether the reported steam rate is not zero. */
    public boolean steamRateNonZero() {
        return steamRateNonZero;
    }

    /** Whether the level measuring device is currently out of order. */
    public boolean levelDeviceOutOfOrder() {
        return levelDeviceOutOfOrder;
    }

    /** Whether a unit other than the level measuring device is currently out of order. */
    public boolean otherUnitOutOfOrder() {
        return otherUnitOutOfOrder;
    }

    /** Whether the steam measuring device or a pump controller is out of order. */
    public boolean rescueEstimateUnreliable() {
        return rescueEstimateUnreliable;
    }

    /** Whether the level reading q is above N2. */
    public boolean levelAboveNormal() {
        return levelAboveNormal;
    }

    /** Whether the level reading q is below N1. */
    public boolean levelBelowNormal() {
        return levelBelowNormal;
    }

    /** Whether, under worst-case dynamics, the level could pass M1 or M2 by the next cycle. */
    public boolean levelLimitRisk() {
        return levelLimitRisk;
    }

    /** Whether the worst-case level at the next cycle, with the pumps kept, is below N1. */
    public boolean levelHeadingLow() {
        return levelHeadingLow;
    }

    /** Whether the best-case level at the next cycle, with the pumps kept, is above N2. */
    public boolean levelHeadingHigh() {
        return levelHeadingHigh;
    }
}
