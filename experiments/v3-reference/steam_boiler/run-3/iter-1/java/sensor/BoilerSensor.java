package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.monitor.PlantMonitor;

/**
 * The quantities the mode controller reads in each cycle: the content of the incoming
 * transmission and the plant analysis of the {@link PlantMonitor} (SB-V3).
 */
public final class BoilerSensor {

    private final PlantMonitor monitor;

    /** Reads the results of the given monitor. */
    public BoilerSensor(PlantMonitor monitor) {
        this.monitor = monitor;
    }

    /** Successive transmissions, up to this one, that carried STOP (SB-V10, SB-Beh23). */
    @RoboChartType("nat")
    public int successiveStops() {
        return monitor.successiveStopCount();
    }

    /** The transmission system has failed in this cycle (SB-FR5). */
    public boolean transmissionFailure() {
        return monitor.transmissionFailed();
    }

    /** STEAM_BOILER_WAITING is in this transmission (SB-Beh2). */
    public boolean steamBoilerWaitingReceived() {
        return monitor.steamBoilerWaitingReceived();
    }

    /** PHYSICAL_UNITS_READY is in this transmission (SB-Beh8). */
    public boolean physicalUnitsReadyReceived() {
        return monitor.physicalUnitsReadyReceived();
    }

    /** The level measuring device is out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceDefective() {
        return monitor.levelDeviceDefective();
    }

    /** The steam measuring device is out of order (SB-V9, SB-FR4). */
    public boolean steamDeviceDefective() {
        return monitor.steamDeviceDefective();
    }

    /** At least one pump is out of order (SB-V9, SB-FR1). */
    public boolean pumpDefective() {
        return monitor.pumpDefective();
    }

    /** At least one pump controller is out of order (SB-V9, SB-FR2). */
    public boolean pumpControllerDefective() {
        return monitor.pumpControllerDefective();
    }

    /** The reported steam rate is zero (SB-Beh3). */
    public boolean steamRateZero() {
        return monitor.steamRateZero();
    }

    /** The water level is below N1 (SB-Beh5). */
    public boolean levelBelowNormal() {
        return monitor.levelBelowNormal();
    }

    /** The water level is above N2 (SB-Beh4). */
    public boolean levelAboveNormal() {
        return monitor.levelAboveNormal();
    }

    /** The level expected at the end of the coming cycle is below N1 (SB-Beh10, SB-Beh19). */
    public boolean predictedLevelBelowNormal() {
        return monitor.predictedLevelBelowNormal();
    }

    /** The level expected at the end of the coming cycle is above N2 (SB-Beh10, SB-Beh19). */
    public boolean predictedLevelAboveNormal() {
        return monitor.predictedLevelAboveNormal();
    }

    /** The level risks reaching M1 or M2 under worst-case behaviour (SB-V4, SB-Beh13). */
    public boolean levelLimitRisk() {
        return monitor.levelLimitRisk();
    }
}
