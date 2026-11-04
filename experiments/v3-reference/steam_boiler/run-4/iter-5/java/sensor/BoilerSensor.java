package steam_boiler.sensor;

/**
 * The plant judgements the controller's guards read, one boolean per
 * condition. They are computed each cycle by the plant monitor from the
 * analysed transmission and set here through {@link #update}.
 */
public final class BoilerSensor {

    private boolean initialisationEmergency = false;
    private boolean operationEmergency = false;
    private boolean rescueEmergency = false;
    private boolean steamBoilerWaitingReceived = false;
    private boolean physicalUnitsReadyReceived = false;
    private boolean levelUnitFailed = false;
    private boolean nonLevelUnitFailed = false;
    private boolean levelUnderNormalBand = false;
    private boolean levelOverNormalBand = false;

    /** Sets the judgements of the current cycle. */
    public void update(boolean initialisationEmergency, boolean operationEmergency, boolean rescueEmergency,
            boolean steamBoilerWaitingReceived, boolean physicalUnitsReadyReceived, boolean levelUnitFailed,
            boolean nonLevelUnitFailed, boolean levelUnderNormalBand, boolean levelOverNormalBand) {
        this.initialisationEmergency = initialisationEmergency;
        this.operationEmergency = operationEmergency;
        this.rescueEmergency = rescueEmergency;
        this.steamBoilerWaitingReceived = steamBoilerWaitingReceived;
        this.physicalUnitsReadyReceived = physicalUnitsReadyReceived;
        this.levelUnitFailed = levelUnitFailed;
        this.nonLevelUnitFailed = nonLevelUnitFailed;
        this.levelUnderNormalBand = levelUnderNormalBand;
        this.levelOverNormalBand = levelOverNormalBand;
    }

    /**
     * Emergency stop is due in initialization (SB-Beh3, SB-Beh6, SB-Beh9,
     * SB-Beh23): STOP on three successive transmissions, a transmission
     * failure, or, once the plant is waiting, a defective level device or a
     * steam device that is defective or reports a non-zero rate.
     */
    public boolean initialisationEmergency() {
        return initialisationEmergency;
    }

    /**
     * Emergency stop is due in normal or degraded mode (SB-Beh13, SB-Beh14,
     * SB-Beh18, SB-Beh23, SB-FR8): STOP on three successive transmissions, a
     * transmission failure, a level at risk of reaching M1 or M2, or a level
     * device failure while the steam device or a pump controller is out of
     * order.
     */
    public boolean operationEmergency() {
        return operationEmergency;
    }

    /**
     * Emergency stop is due in rescue mode (SB-Beh21, SB-Beh23): STOP on three
     * successive transmissions, a transmission failure, an estimated level at
     * risk of reaching M1 or M2, or the steam device or a pump controller out
     * of order.
     */
    public boolean rescueEmergency() {
        return rescueEmergency;
    }

    /** SB-Beh2: STEAM_BOILER_WAITING has arrived during initialization. */
    public boolean steamBoilerWaitingReceived() {
        return steamBoilerWaitingReceived;
    }

    /** SB-Beh8: PHYSICAL_UNITS_READY is part of the current transmission. */
    public boolean physicalUnitsReadyReceived() {
        return physicalUnitsReadyReceived;
    }

    /** SB-FR3, SB-V9: the level measuring device is out of order. */
    public boolean levelUnitFailed() {
        return levelUnitFailed;
    }

    /** SB-V9: some unit other than the level measuring device (a pump, a pump controller, the steam device) is out of order. */
    public boolean nonLevelUnitFailed() {
        return nonLevelUnitFailed;
    }

    /** SB-Beh4, SB-Beh5, SB-Beh10: the water level (or, in rescue, its estimate) is below N1. */
    public boolean levelUnderNormalBand() {
        return levelUnderNormalBand;
    }

    /** SB-Beh4, SB-Beh10: the water level (or, in rescue, its estimate) is above N2. */
    public boolean levelOverNormalBand() {
        return levelOverNormalBand;
    }
}
