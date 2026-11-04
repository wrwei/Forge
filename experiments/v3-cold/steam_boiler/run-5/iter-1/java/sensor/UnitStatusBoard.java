package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.event.OutputEvent;

/**
 * Status of every physical unit: out of order or sound, and whether the failure detection
 * announced for it still awaits its acknowledgement. Units are numbered 0..9: pumps 0..3,
 * pump controllers 4..7, the level device 8 and the steam device 9.
 */
final class UnitStatusBoard {

    static final int LEVEL_UNIT = 2 * BoilerConstants.PUMP_COUNT;
    static final int STEAM_UNIT = LEVEL_UNIT + 1;
    private static final int UNIT_COUNT = STEAM_UNIT + 1;

    private final boolean[] outOfOrder = new boolean[UNIT_COUNT];
    private final boolean[] awaitingAcknowledgement = new boolean[UNIT_COUNT];
    private final boolean[] repairedThisCycle = new boolean[UNIT_COUNT];

    static int pumpUnit(int pumpIndex) {
        return pumpIndex;
    }

    static int pumpControlUnit(int pumpIndex) {
        return BoilerConstants.PUMP_COUNT + pumpIndex;
    }

    void beginCycle() {
        for (int unit = 0; unit < UNIT_COUNT; unit++) {
            repairedThisCycle[unit] = false;
        }
    }

    boolean outOfOrder(int unit) {
        return outOfOrder[unit];
    }

    boolean awaitingAcknowledgement(int unit) {
        return awaitingAcknowledgement[unit];
    }

    boolean repairedThisCycle(int unit) {
        return repairedThisCycle[unit];
    }

    /** The program has detected the failure of the unit: out of order, announcement pending. */
    void detectFailure(int unit) {
        if (!outOfOrder[unit]) {
            outOfOrder[unit] = true;
            awaitingAcknowledgement[unit] = true;
        }
    }

    /** The unit acknowledged the failure detection: stop repeating the announcement. */
    void acknowledgeFailure(int unit) {
        awaitingAcknowledgement[unit] = false;
    }

    /** The unit reported itself repaired: sound again, acknowledged in this cycle. */
    void repair(int unit) {
        outOfOrder[unit] = false;
        awaitingAcknowledgement[unit] = false;
        repairedThisCycle[unit] = true;
    }

    boolean anyPumpOutOfOrder() {
        boolean found = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (outOfOrder[pumpUnit(i)]) {
                found = true;
            }
        }
        return found;
    }

    boolean anyPumpControlOutOfOrder() {
        boolean found = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (outOfOrder[pumpControlUnit(i)]) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Messages due in the current cycle: each failure detection not yet acknowledged, and the
     * acknowledgement of each repair reported in this cycle.
     */
    List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            int pumpNumber = i + 1;
            if (awaitingAcknowledgement[pumpUnit(i)]) {
                messages.add(new OutputEvent.PumpFailureDetection(pumpNumber));
            }
            if (awaitingAcknowledgement[pumpControlUnit(i)]) {
                messages.add(new OutputEvent.PumpControlFailureDetection(pumpNumber));
            }
            if (repairedThisCycle[pumpUnit(i)]) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(pumpNumber));
            }
            if (repairedThisCycle[pumpControlUnit(i)]) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(pumpNumber));
            }
        }
        if (awaitingAcknowledgement[LEVEL_UNIT]) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (awaitingAcknowledgement[STEAM_UNIT]) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (repairedThisCycle[LEVEL_UNIT]) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (repairedThisCycle[STEAM_UNIT]) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return messages;
    }
}
