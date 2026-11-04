package steam_boiler.sensor;

import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.data.UnitCondition;

/**
 * Per-unit bookkeeping (SB-V9): for each of the four pumps, the four pump controllers, the
 * level measuring device and the steam measuring device, whether the unit is believed sound
 * or out of order and whether its failure detection has been acknowledged.
 *
 * <p>Units are numbered 0..3 for pumps 1..4, 4..7 for the controllers of pumps 1..4, then the
 * level device and the steam device.
 */
final class UnitStatusTable {

    static final int LEVEL_UNIT = 2 * SteamBoilerConstants.PUMP_COUNT;
    static final int STEAM_UNIT = LEVEL_UNIT + 1;
    static final int UNIT_COUNT = STEAM_UNIT + 1;

    private final UnitCondition[] conditions = new UnitCondition[UNIT_COUNT];

    UnitStatusTable() {
        for (int unit = 0; unit < UNIT_COUNT; unit++) {
            conditions[unit] = UnitCondition.SOUND;
        }
    }

    static int pumpUnit(int pump) {
        return pump - 1;
    }

    static int pumpControllerUnit(int pump) {
        return SteamBoilerConstants.PUMP_COUNT + pump - 1;
    }

    static boolean isPumpUnit(int unit) {
        return unit < SteamBoilerConstants.PUMP_COUNT;
    }

    static boolean isPumpControllerUnit(int unit) {
        return unit >= SteamBoilerConstants.PUMP_COUNT && unit < LEVEL_UNIT;
    }

    /** The pump number (1..4) a pump or pump-controller unit refers to. */
    static int pumpOf(int unit) {
        if (isPumpUnit(unit)) {
            return unit + 1;
        }
        return unit - SteamBoilerConstants.PUMP_COUNT + 1;
    }

    boolean outOfOrder(int unit) {
        return conditions[unit] != UnitCondition.SOUND;
    }

    boolean awaitingAcknowledgement(int unit) {
        return conditions[unit] == UnitCondition.FAILED_UNACKNOWLEDGED;
    }

    boolean anyPumpOutOfOrder() {
        boolean found = false;
        for (int pump = 1; pump <= SteamBoilerConstants.PUMP_COUNT; pump++) {
            if (outOfOrder(pumpUnit(pump))) {
                found = true;
            }
        }
        return found;
    }

    boolean anyPumpControllerOutOfOrder() {
        boolean found = false;
        for (int pump = 1; pump <= SteamBoilerConstants.PUMP_COUNT; pump++) {
            if (outOfOrder(pumpControllerUnit(pump))) {
                found = true;
            }
        }
        return found;
    }

    /** A failure of a sound unit has been detected: it becomes out of order, unacknowledged. */
    void detect(int unit) {
        if (conditions[unit] == UnitCondition.SOUND) {
            conditions[unit] = UnitCondition.FAILED_UNACKNOWLEDGED;
        }
    }

    /** The unit has acknowledged the announced failure detection. */
    void acknowledge(int unit) {
        if (conditions[unit] == UnitCondition.FAILED_UNACKNOWLEDGED) {
            conditions[unit] = UnitCondition.FAILED_ACKNOWLEDGED;
        }
    }

    /** The unit has reported itself repaired: it is sound again (SB-FR7). */
    void repair(int unit) {
        conditions[unit] = UnitCondition.SOUND;
    }
}
