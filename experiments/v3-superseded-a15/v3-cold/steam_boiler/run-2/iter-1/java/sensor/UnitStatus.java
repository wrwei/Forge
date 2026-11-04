package steam_boiler.sensor;

import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.event.OutputEvent;

/**
 * For each of the ten physical units, whether it is believed sound or out of order, and whether a
 * failure detection announced for it still awaits acknowledgement (SB-V9).
 *
 * <p>Units are indexed 0-3 for pumps 1-4, 4-7 for the controllers of pumps 1-4, then the level
 * measuring device and the steam measuring device.
 */
final class UnitStatus {

    @RoboChartType("nat") static final int PUMP_BASE = 0;
    @RoboChartType("nat") static final int CONTROL_BASE = PUMP_BASE + BoilerConstants.PUMP_COUNT;
    @RoboChartType("nat") static final int LEVEL_UNIT = CONTROL_BASE + BoilerConstants.PUMP_COUNT;
    @RoboChartType("nat") static final int STEAM_UNIT = LEVEL_UNIT + 1;
    @RoboChartType("nat") static final int UNIT_COUNT = STEAM_UNIT + 1;

    private final boolean[] outOfOrder = new boolean[UNIT_COUNT];
    private final boolean[] awaitingAcknowledgement = new boolean[UNIT_COUNT];

    boolean isOutOfOrder(@RoboChartType("nat") int unit) {
        return outOfOrder[unit];
    }

    boolean isAwaitingAcknowledgement(@RoboChartType("nat") int unit) {
        return awaitingAcknowledgement[unit];
    }

    /** A failure of the unit has been detected: out of order, announcement pending (SB-FR6). */
    void recordFailure(@RoboChartType("nat") int unit) {
        outOfOrder[unit] = true;
        awaitingAcknowledgement[unit] = true;
    }

    /** The units acknowledged the failure detection: stop announcing it (SB-FR6). */
    void recordAcknowledgement(@RoboChartType("nat") int unit) {
        awaitingAcknowledgement[unit] = false;
    }

    /** The unit reported itself repaired: sound again (SB-FR7). */
    void recordRepair(@RoboChartType("nat") int unit) {
        outOfOrder[unit] = false;
        awaitingAcknowledgement[unit] = false;
    }

    /** Whether any of {@code count} units starting at {@code first} is out of order. */
    boolean anyOutOfOrder(@RoboChartType("nat") int first, @RoboChartType("nat") int count) {
        boolean found = false;
        for (int unit = first; unit < first + count; unit++) {
            if (outOfOrder[unit]) {
                found = true;
            }
        }
        return found;
    }

    /** Appends the failure detection of every unit still awaiting acknowledgement (SB-FR6). */
    void addAnnouncements(List<OutputEvent> messages) {
        for (int unit = 0; unit < UNIT_COUNT; unit++) {
            if (awaitingAcknowledgement[unit]) {
                messages.add(failureDetection(unit));
            }
        }
    }

    static OutputEvent failureDetection(@RoboChartType("nat") int unit) {
        if (unit < CONTROL_BASE) {
            return new OutputEvent.PumpFailureDetection(unit - PUMP_BASE + 1);
        } else if (unit < LEVEL_UNIT) {
            return new OutputEvent.PumpControlFailureDetection(unit - CONTROL_BASE + 1);
        } else if (unit == LEVEL_UNIT) {
            return new OutputEvent.LevelFailureDetection();
        }
        return new OutputEvent.SteamFailureDetection();
    }

    static OutputEvent repairAcknowledgement(@RoboChartType("nat") int unit) {
        if (unit < CONTROL_BASE) {
            return new OutputEvent.PumpRepairedAcknowledgement(unit - PUMP_BASE + 1);
        } else if (unit < LEVEL_UNIT) {
            return new OutputEvent.PumpControlRepairedAcknowledgement(unit - CONTROL_BASE + 1);
        } else if (unit == LEVEL_UNIT) {
            return new OutputEvent.LevelRepairedAcknowledgement();
        }
        return new OutputEvent.SteamRepairedAcknowledgement();
    }
}
