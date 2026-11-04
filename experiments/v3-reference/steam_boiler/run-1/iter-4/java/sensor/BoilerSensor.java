package steam_boiler.sensor;

/**
 * What the program knows on each cycle, derived from the cycle's incoming
 * transmission by {@link PlantMonitor}, which writes it before the controller
 * is stepped.
 */
public final class BoilerSensor {

    boolean startUpAbort;
    boolean startUpLevelReached;
    boolean unitsReady;
    boolean emergencyCause;
    boolean levelFailure;
    boolean unitFailure;
    boolean estimateUnreliable;
    boolean plantWaiting;
    boolean levelBelowNormal;
    boolean levelAboveNormal;

    /**
     * True when initialisation must end in emergency stop: STOP has arrived on
     * three successive transmissions, or the transmission has failed, or the
     * plant is waiting and either the steam rate is not zero or the level
     * measuring device is out of order.
     */
    public boolean startUpAbort() { return startUpAbort; }

    /**
     * True when the plant is waiting, the steam rate is zero, the level
     * measuring device is sound and the level lies between N1 and N2.
     */
    public boolean startUpLevelReached() { return startUpLevelReached; }

    /** True when this transmission carries PHYSICAL_UNITS_READY. */
    public boolean unitsReady() { return unitsReady; }

    /**
     * True when STOP has arrived on three successive transmissions, or the
     * transmission has failed, or the level could reach M1 or M2 before the
     * next cycle under worst-case steam and pump behaviour.
     */
    public boolean emergencyCause() { return emergencyCause; }

    /** True while the level measuring device is out of order. */
    public boolean levelFailure() { return levelFailure; }

    /** True while the steam measuring device, a pump or a pump controller is out of order. */
    public boolean unitFailure() { return unitFailure; }

    /** True while the steam measuring device or a pump controller is out of order, so no level estimate can be trusted. */
    public boolean estimateUnreliable() { return estimateUnreliable; }

    /** True once STEAM_BOILER_WAITING has arrived, in this or an earlier transmission. */
    public boolean plantWaiting() { return plantWaiting; }

    /** True when the best known water level (reading, or estimate while the device is out of order) is below N1. */
    public boolean levelBelowNormal() { return levelBelowNormal; }

    /** True when the best known water level is above N2. */
    public boolean levelAboveNormal() { return levelAboveNormal; }
}
