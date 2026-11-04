package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

/**
 * The quantities the controller reads each cycle, read from or derived from
 * the physical units' messages after the analysis stage (SB-V5 to SB-V10,
 * SB-FR1 to SB-FR5). Every method is a pure query of the current cycle's
 * analysis held by the {@link PlantMonitor}.
 */
public final class BoilerSensor {

    private final PlantMonitor monitor;

    /**
     * Creates the sensor view of a monitor.
     *
     * @param monitor the receive and analysis stage it reads from
     */
    public BoilerSensor(PlantMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Number of successive transmissions that carried STOP, this one included (SB-V10).
     *
     * @return the count, zero if this transmission had no STOP
     */
    @RoboChartType("nat")
    public int successiveStopCount() {
        return monitor.successiveStopCount();
    }

    /**
     * Whether this transmission had an aberrant message or lacked an indispensable one (SB-FR5).
     *
     * @return {@code true} on transmission failure
     */
    public boolean transmissionFailure() {
        return monitor.transmissionFailed();
    }

    /**
     * Whether STEAM_BOILER_WAITING has arrived, in this or an earlier cycle (SB-Beh2).
     *
     * @return {@code true} once the plant has signalled it is waiting
     */
    public boolean steamBoilerWaitingReceived() {
        return monitor.plantWaiting();
    }

    /**
     * Whether this transmission carried PHYSICAL_UNITS_READY (SB-Beh8).
     *
     * @return {@code true} if the units answered PROGRAM_READY
     */
    public boolean physicalUnitsReadyReceived() {
        return monitor.unitsReady();
    }

    /**
     * v: the steam rate most recently reported (litre/sec) (SB-V6).
     *
     * @return the reported steam rate
     */
    @RoboChartType("real")
    public double steamRate() {
        return monitor.steamQuantity();
    }

    /**
     * q: the water level most recently reported (litre) (SB-V5).
     *
     * @return the reported level
     */
    @RoboChartType("real")
    public double levelReading() {
        return monitor.levelQuantity();
    }

    /**
     * p: current throughput of the pumps taken together (litre/sec) (SB-V7).
     *
     * @return the throughput derived from pump and pump-controller states
     */
    @RoboChartType("real")
    public double pumpThroughput() {
        return monitor.pumpQuantity();
    }

    /**
     * Whether the level measuring device is out of order (SB-V9, SB-FR3).
     *
     * @return {@code true} if out of order
     */
    public boolean levelDeviceOutOfOrder() {
        return monitor.levelOutOfOrder();
    }

    /**
     * Whether the steam measuring device is out of order (SB-V9, SB-FR4).
     *
     * @return {@code true} if out of order
     */
    public boolean steamDeviceOutOfOrder() {
        return monitor.steamOutOfOrder();
    }

    /**
     * Whether some pump is out of order (SB-V9, SB-FR1).
     *
     * @return {@code true} if at least one pump is out of order
     */
    public boolean pumpOutOfOrder() {
        return monitor.anyPumpOutOfOrder();
    }

    /**
     * Whether some pump controller is out of order (SB-V9, SB-FR2).
     *
     * @return {@code true} if at least one pump controller is out of order
     */
    public boolean pumpControlOutOfOrder() {
        return monitor.anyControlOutOfOrder();
    }

    /**
     * Whether the evacuation valve is open, as last requested by VALVE (SB-DM7, SB-Beh4).
     *
     * @return {@code true} if open
     */
    public boolean valveOpen() {
        return monitor.valveIsOpen();
    }

    /**
     * Number of pumps reporting themselves on in this transmission (SB-DM3).
     *
     * @return 0 to 4
     */
    @RoboChartType("nat")
    public int openPumpCount() {
        return monitor.openPumpCount();
    }

    /**
     * Level used for regulation: q while the level device is sound, otherwise
     * the middle of the estimate (SB-Beh10, SB-Beh19).
     *
     * @return the reference level (litre)
     */
    @RoboChartType("real")
    public double waterLevel() {
        return monitor.referenceLevel();
    }

    /**
     * Lower bound on the true level: q while the level device is sound,
     * otherwise the estimated lower bound (SB-V8).
     *
     * @return the lower bound (litre)
     */
    @RoboChartType("real")
    public double levelLowerBound() {
        return monitor.lowerBound();
    }

    /**
     * Upper bound on the true level: q while the level device is sound,
     * otherwise the estimated upper bound (SB-V8).
     *
     * @return the upper bound (litre)
     */
    @RoboChartType("real")
    public double levelUpperBound() {
        return monitor.upperBound();
    }

    /**
     * Most steam that can leave during the next cycle, given v, U1 and W, or W
     * for the whole cycle if the steam device is not trusted (SB-DM2, SB-Beh13).
     *
     * @return litres
     */
    @RoboChartType("real")
    public double maximumSteamOutflow() {
        return monitor.nextSteamOutflowMax();
    }

    /**
     * Least steam that can leave during the next cycle, given v and U2, or
     * none if the steam device is not trusted (SB-DM2, SB-Beh13).
     *
     * @return litres
     */
    @RoboChartType("real")
    public double minimumSteamOutflow() {
        return monitor.nextSteamOutflowMin();
    }

    /**
     * Most water the pumps can pour during the next cycle if no pump is closed:
     * pumps now on (or out of order) pour P each; a pump ordered on in this
     * cycle pours nothing during it (SB-DM3, SB-Beh25).
     *
     * @return litres
     */
    @RoboChartType("real")
    public double maximumPumpInflow() {
        return monitor.nextPumpInflowMax();
    }
}
