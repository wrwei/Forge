package steam_boiler.sensor;

import steam_boiler.monitor.PlantMonitor;

/**
 * The facts the mode controller reads in each cycle, from the analysis of the incoming
 * transmission by the {@link PlantMonitor} (SB-V3).
 */
public final class BoilerSensor {

    private final PlantMonitor monitor;

    /** Reads the results of the given monitor. */
    public BoilerSensor(PlantMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Initialization must end in emergency stop: operator stop, transmission failure, or a
     * failed start-up check (SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23).
     */
    public boolean initialisationAbort() {
        return monitor.initialisationAbort();
    }

    /** PHYSICAL_UNITS_READY is in this transmission (SB-Beh8). */
    public boolean physicalUnitsReadyReceived() {
        return monitor.physicalUnitsReadyReceived();
    }

    /**
     * Emergency stop is demanded in normal, degraded or rescue mode: operator stop,
     * transmission failure, or a level risking M1 or M2 (SB-Beh13, SB-Beh14, SB-Beh18,
     * SB-Beh21, SB-Beh23).
     */
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
}
