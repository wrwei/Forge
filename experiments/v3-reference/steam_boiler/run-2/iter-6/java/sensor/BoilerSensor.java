package steam_boiler.sensor;

/**
 * Sensor service: what the program knows about the plant in the current cycle,
 * once the incoming transmission has been received and analysed. The unit
 * monitor fills it before the controller is stepped; the controller only reads
 * it. Every quantity is a yes/no fact: the comparisons of the level reading
 * with N1 and N2 are made by the unit monitor.
 */
public final class BoilerSensor {

    private boolean levelDeviceOutOfOrder;
    private boolean otherUnitOutOfOrder;
    private boolean levelBelowNormal;
    private boolean levelAboveNormal;

    /** Records which units are currently believed out of order. */
    public void updateUnits(boolean levelDeviceOutOfOrder, boolean otherUnitOutOfOrder) {
        this.levelDeviceOutOfOrder = levelDeviceOutOfOrder;
        this.otherUnitOutOfOrder = otherUnitOutOfOrder;
    }

    /** Records how the level reading of the current cycle compares with N1 and N2. */
    public void updateLevels(boolean levelBelowNormal, boolean levelAboveNormal) {
        this.levelBelowNormal = levelBelowNormal;
        this.levelAboveNormal = levelAboveNormal;
    }

    /** The water-level measuring device is believed out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceOutOfOrder() { return levelDeviceOutOfOrder; }

    /** A unit other than the level device (a pump, a pump controller or the steam device) is believed out of order (SB-V9). */
    public boolean otherUnitOutOfOrder() { return otherUnitOutOfOrder; }

    /** The level reading q is below N1 (SB-V5). */
    public boolean levelBelowNormal() { return levelBelowNormal; }

    /** The level reading q is above N2, so not below N1 (SB-V5). */
    public boolean levelAboveNormal() { return levelAboveNormal; }
}
