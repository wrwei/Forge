package steam_boiler.sensor;

/**
 * Sensor service: what the program knows about the plant in the current cycle,
 * once the incoming transmission has been received and analysed. The unit
 * monitor fills it before the controller is stepped; the controller only reads
 * it. Every quantity is a yes/no fact: the comparisons of readings, estimates
 * and predictions with N1 and N2 are made by the unit monitor.
 */
public final class BoilerSensor {

    private boolean levelDeviceOutOfOrder;
    private boolean otherUnitOutOfOrder;
    private boolean levelTooLow;
    private boolean levelTooHigh;

    /** Records which units are currently believed out of order. */
    public void updateUnits(boolean levelDeviceOutOfOrder, boolean otherUnitOutOfOrder) {
        this.levelDeviceOutOfOrder = levelDeviceOutOfOrder;
        this.otherUnitOutOfOrder = otherUnitOutOfOrder;
    }

    /** Records how the level of the current cycle compares with the normal quantities N1 and N2. */
    public void updateLevels(boolean levelTooLow, boolean levelTooHigh) {
        this.levelTooLow = levelTooLow;
        this.levelTooHigh = levelTooHigh;
    }

    /** The water-level measuring device is believed out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceOutOfOrder() { return levelDeviceOutOfOrder; }

    /** A unit other than the level device (a pump, a pump controller or the steam device) is believed out of order (SB-V9). */
    public boolean otherUnitOutOfOrder() { return otherUnitOutOfOrder; }

    /**
     * The level needs raising: in initialisation the reading q is below N1;
     * in the other modes the level could fall below N1 before the next
     * transmission with the open pumps left open (SB-V5, SB-Beh25).
     */
    public boolean levelTooLow() { return levelTooLow; }

    /**
     * The level needs lowering, and not raising: in initialisation the
     * reading q is above N2; in the other modes the level could rise above N2
     * before the next transmission with the open pumps left open (SB-V5, SB-Beh25).
     */
    public boolean levelTooHigh() { return levelTooHigh; }
}
