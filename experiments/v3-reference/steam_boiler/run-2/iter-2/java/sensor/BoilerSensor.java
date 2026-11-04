package steam_boiler.sensor;

/**
 * Sensor service: what the program knows about the plant in the current cycle,
 * once the incoming transmission has been received and analysed. The unit
 * monitor fills it before the controller is stepped; the controller only reads
 * it. Every quantity is a yes/no fact: the comparisons of readings, estimates
 * and predictions with N1, N2, M1 and M2 are made by the unit monitor.
 */
public final class BoilerSensor {

    private boolean operatorStopConfirmed;
    private boolean transmissionFailure;
    private boolean steamBoilerWaitingReceived;
    private boolean physicalUnitsReadyReceived;
    private boolean levelDeviceOutOfOrder;
    private boolean steamDeviceOutOfOrder;
    private boolean pumpControllerOutOfOrder;
    private boolean otherUnitOutOfOrder;
    private boolean levelBelowNormal;
    private boolean levelAboveNormal;
    private boolean levelMayFallBelowNormal;
    private boolean levelMayRiseAboveNormal;
    private boolean levelRiskOfLimit;

    /** Records the message-level facts of the current cycle. */
    public void updateMessages(boolean operatorStopConfirmed, boolean transmissionFailure,
            boolean steamBoilerWaitingReceived, boolean physicalUnitsReadyReceived) {
        this.operatorStopConfirmed = operatorStopConfirmed;
        this.transmissionFailure = transmissionFailure;
        this.steamBoilerWaitingReceived = steamBoilerWaitingReceived;
        this.physicalUnitsReadyReceived = physicalUnitsReadyReceived;
    }

    /** Records which units are currently believed out of order. */
    public void updateUnits(boolean levelDeviceOutOfOrder, boolean steamDeviceOutOfOrder,
            boolean pumpControllerOutOfOrder, boolean otherUnitOutOfOrder) {
        this.levelDeviceOutOfOrder = levelDeviceOutOfOrder;
        this.steamDeviceOutOfOrder = steamDeviceOutOfOrder;
        this.pumpControllerOutOfOrder = pumpControllerOutOfOrder;
        this.otherUnitOutOfOrder = otherUnitOutOfOrder;
    }

    /** Records how the level of the current cycle compares with the normal and limit quantities. */
    public void updateLevels(boolean levelBelowNormal, boolean levelAboveNormal,
            boolean levelMayFallBelowNormal, boolean levelMayRiseAboveNormal, boolean levelRiskOfLimit) {
        this.levelBelowNormal = levelBelowNormal;
        this.levelAboveNormal = levelAboveNormal;
        this.levelMayFallBelowNormal = levelMayFallBelowNormal;
        this.levelMayRiseAboveNormal = levelMayRiseAboveNormal;
        this.levelRiskOfLimit = levelRiskOfLimit;
    }

    /** STOP has arrived on three successive transmissions (SB-V10, SB-Beh23). */
    public boolean operatorStopConfirmed() { return operatorStopConfirmed; }

    /** The transmission system has failed in this cycle (SB-FR5). */
    public boolean transmissionFailure() { return transmissionFailure; }

    /** STEAM_BOILER_WAITING is part of this cycle's transmission. */
    public boolean steamBoilerWaitingReceived() { return steamBoilerWaitingReceived; }

    /** PHYSICAL_UNITS_READY is part of this cycle's transmission. */
    public boolean physicalUnitsReadyReceived() { return physicalUnitsReadyReceived; }

    /** The water-level measuring device is believed out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceOutOfOrder() { return levelDeviceOutOfOrder; }

    /** The steam measuring device is believed out of order (SB-V9, SB-FR4, SB-Beh3). */
    public boolean steamDeviceOutOfOrder() { return steamDeviceOutOfOrder; }

    /** At least one pump controller is believed out of order (SB-V9, SB-FR2). */
    public boolean pumpControllerOutOfOrder() { return pumpControllerOutOfOrder; }

    /** A unit other than the level device (a pump, a pump controller or the steam device) is believed out of order (SB-V9). */
    public boolean otherUnitOutOfOrder() { return otherUnitOutOfOrder; }

    /** The level reading q is below N1 (SB-V5). */
    public boolean levelBelowNormal() { return levelBelowNormal; }

    /** The level reading q is above N2 (SB-V5). */
    public boolean levelAboveNormal() { return levelAboveNormal; }

    /** With the open pumps left open, the level could fall below N1 before the next transmission (SB-Beh25). */
    public boolean levelMayFallBelowNormal() { return levelMayFallBelowNormal; }

    /** The level cannot fall below N1 but could rise above N2 before the next transmission with the open pumps left open (SB-Beh25). */
    public boolean levelMayRiseAboveNormal() { return levelMayRiseAboveNormal; }

    /**
     * The level risks reaching M1 or M2 before the next transmission under
     * worst-case behaviour, given this cycle's pump decision: pumps closed if
     * the level could rise above N2, left as they are otherwise (SB-V4, SB-Beh13).
     */
    public boolean levelRiskOfLimit() { return levelRiskOfLimit; }
}
