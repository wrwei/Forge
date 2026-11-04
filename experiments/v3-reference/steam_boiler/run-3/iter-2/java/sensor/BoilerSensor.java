package steam_boiler.sensor;

import steam_boiler.monitor.PlantMonitor;

/**
 * The facts the mode controller reads in each cycle: the content of the incoming
 * transmission and the plant analysis of the {@link PlantMonitor} (SB-V3).
 */
public final class BoilerSensor {

    private final PlantMonitor monitor;

    /** Reads the results of the given monitor. */
    public BoilerSensor(PlantMonitor monitor) {
        this.monitor = monitor;
    }

    /** STOP on three successive transmissions (SB-Beh23), or a transmission failure (SB-FR5). */
    public boolean stopOrTransmissionFailure() {
        return monitor.stopOrTransmissionFailure();
    }

    /** STEAM_BOILER_WAITING has arrived, now or earlier (SB-Beh2). */
    public boolean steamBoilerWaitingReceived() {
        return monitor.steamBoilerWaitingReceived();
    }

    /** PHYSICAL_UNITS_READY is in this transmission (SB-Beh8). */
    public boolean physicalUnitsReadyReceived() {
        return monitor.physicalUnitsReadyReceived();
    }

    /** The steam rate is not zero or the level device is out of order (SB-Beh3, SB-Beh6). */
    public boolean startupCheckFailed() {
        return monitor.startupCheckFailed();
    }

    /** Emergency stop is demanded in normal, degraded or rescue mode (SB-Beh13, SB-Beh14, SB-Beh18, SB-Beh21, SB-Beh23). */
    public boolean operatingEmergency() {
        return monitor.operatingEmergency();
    }

    /** The level measuring device is out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceDefective() {
        return monitor.levelDeviceDefective();
    }

    /** Some unit other than the level measuring device is out of order (SB-V9). */
    public boolean otherUnitDefective() {
        return monitor.otherUnitDefective();
    }

    /** The steam device or a pump controller is out of order (SB-Beh19, SB-Beh21). */
    public boolean estimateUnreliable() {
        return monitor.estimateUnreliable();
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
}
