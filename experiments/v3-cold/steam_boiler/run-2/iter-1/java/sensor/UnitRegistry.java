package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.event.OutputEvent;

/**
 * Bookkeeping for each physical unit: whether it is believed sound or out of order,
 * whether the detection of its failure still awaits acknowledgement, and whether a
 * repair it reported this cycle must be acknowledged. Units 0 to 3 are the pumps,
 * 4 to 7 their controllers, then the level device and the steam device.
 */
final class UnitRegistry {
    static final int PUMPS = BoilerConstants.PUMP_COUNT;
    static final int LEVEL_UNIT = 2 * PUMPS;
    static final int STEAM_UNIT = 2 * PUMPS + 1;
    static final int UNIT_COUNT = 2 * PUMPS + 2;

    private final boolean[] failed = new boolean[UNIT_COUNT];
    private final boolean[] awaitingAcknowledgement = new boolean[UNIT_COUNT];
    private final boolean[] repairedThisCycle = new boolean[UNIT_COUNT];

    static int pumpUnit(int i) { return i; }

    static int controllerUnit(int i) { return PUMPS + i; }

    void startCycle() {
        for (int u = 0; u < UNIT_COUNT; u++) {
            repairedThisCycle[u] = false;
        }
    }

    /** The unit acknowledged the program's failure detection: stop repeating it. */
    void acknowledgeFailure(int unit) {
        awaitingAcknowledgement[unit] = false;
    }

    /** The unit reported itself repaired: it is sound again and the repair is acknowledged. */
    void repair(int unit) {
        failed[unit] = false;
        awaitingAcknowledgement[unit] = false;
        repairedThisCycle[unit] = true;
    }

    /** The program detected a failure of the unit: it is out of order and announced. */
    void detectFailure(int unit) {
        if (!failed[unit]) {
            failed[unit] = true;
            awaitingAcknowledgement[unit] = true;
        }
    }

    boolean isFailed(int unit) { return failed[unit]; }

    boolean repairedThisCycle(int unit) { return repairedThisCycle[unit]; }

    boolean anyPumpFailed() {
        boolean any = false;
        for (int i = 0; i < PUMPS; i++) {
            any = any || failed[pumpUnit(i)];
        }
        return any;
    }

    boolean anyControllerFailed() {
        boolean any = false;
        for (int i = 0; i < PUMPS; i++) {
            any = any || failed[controllerUnit(i)];
        }
        return any;
    }

    /**
     * The failure detections still awaiting acknowledgement and the acknowledgements of
     * the repairs reported this cycle, in unit order.
     */
    List<OutputEvent> announcements() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int i = 0; i < PUMPS; i++) {
            if (awaitingAcknowledgement[pumpUnit(i)]) {
                messages.add(new OutputEvent.PumpFailureDetection(i + 1));
            }
            if (awaitingAcknowledgement[controllerUnit(i)]) {
                messages.add(new OutputEvent.PumpControlFailureDetection(i + 1));
            }
        }
        if (awaitingAcknowledgement[LEVEL_UNIT]) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (awaitingAcknowledgement[STEAM_UNIT]) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        for (int i = 0; i < PUMPS; i++) {
            if (repairedThisCycle[pumpUnit(i)]) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(i + 1));
            }
            if (repairedThisCycle[controllerUnit(i)]) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(i + 1));
            }
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
