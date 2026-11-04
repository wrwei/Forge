package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.event.InputEvent;

/**
 * The messages of one incoming transmission. All messages of a cycle are taken to be
 * received simultaneously; the buffer is cleared at the start of each cycle.
 */
final class TransmissionBuffer {
    private static final int PUMPS = BoilerConstants.PUMP_COUNT;

    private boolean stopReceived;
    private boolean steamBoilerWaiting;
    private boolean physicalUnitsReady;
    private int levelCount;
    @RoboChartType("real") private double levelValue;
    private int steamCount;
    @RoboChartType("real") private double steamValue;
    private final int[] pumpStateCount = new int[PUMPS];
    private final boolean[] pumpRunning = new boolean[PUMPS];
    private final int[] flowCount = new int[PUMPS];
    private final boolean[] flowing = new boolean[PUMPS];
    private final boolean[] pumpRepaired = new boolean[PUMPS];
    private final boolean[] pumpControlRepaired = new boolean[PUMPS];
    private final boolean[] pumpFailureAcknowledged = new boolean[PUMPS];
    private final boolean[] pumpControlFailureAcknowledged = new boolean[PUMPS];
    private boolean levelRepaired;
    private boolean steamRepaired;
    private boolean levelFailureAcknowledged;
    private boolean steamFailureAcknowledged;
    private boolean duplicate;
    private boolean badPumpNumber;

    void clear() {
        stopReceived = false;
        steamBoilerWaiting = false;
        physicalUnitsReady = false;
        levelCount = 0;
        steamCount = 0;
        for (int i = 0; i < PUMPS; i++) {
            pumpStateCount[i] = 0;
            flowCount[i] = 0;
            pumpRepaired[i] = false;
            pumpControlRepaired[i] = false;
            pumpFailureAcknowledged[i] = false;
            pumpControlFailureAcknowledged[i] = false;
        }
        levelRepaired = false;
        steamRepaired = false;
        levelFailureAcknowledged = false;
        steamFailureAcknowledged = false;
        duplicate = false;
        badPumpNumber = false;
    }

    void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            duplicate = duplicate || stopReceived;
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            duplicate = duplicate || steamBoilerWaiting;
            steamBoilerWaiting = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            duplicate = duplicate || physicalUnitsReady;
            physicalUnitsReady = true;
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            levelCount = levelCount + 1;
            levelValue = level.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            steamCount = steamCount + 1;
            steamValue = steam.value();
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            int i = index(state.value().pump());
            if (i >= 0) {
                pumpStateCount[i] = pumpStateCount[i] + 1;
                pumpRunning[i] = state.value().running();
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState state = (InputEvent.PumpControlState) message;
            int i = index(state.value().pump());
            if (i >= 0) {
                flowCount[i] = flowCount[i] + 1;
                flowing[i] = state.value().flowing();
            }
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            int i = index(repaired.value());
            if (i >= 0) {
                duplicate = duplicate || pumpRepaired[i];
                pumpRepaired[i] = true;
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            int i = index(repaired.value());
            if (i >= 0) {
                duplicate = duplicate || pumpControlRepaired[i];
                pumpControlRepaired[i] = true;
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            duplicate = duplicate || levelRepaired;
            levelRepaired = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            duplicate = duplicate || steamRepaired;
            steamRepaired = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            int i = index(ack.value());
            if (i >= 0) {
                duplicate = duplicate || pumpFailureAcknowledged[i];
                pumpFailureAcknowledged[i] = true;
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            int i = index(ack.value());
            if (i >= 0) {
                duplicate = duplicate || pumpControlFailureAcknowledged[i];
                pumpControlFailureAcknowledged[i] = true;
            }
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            duplicate = duplicate || levelFailureAcknowledged;
            levelFailureAcknowledged = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            duplicate = duplicate || steamFailureAcknowledged;
            steamFailureAcknowledged = true;
        }
    }

    private int index(int pump) {
        if (pump < 1 || pump > PUMPS) {
            badPumpNumber = true;
            return -1;
        }
        return pump - 1;
    }

    /** True when each of the four data messages is present exactly once per unit. */
    boolean dataComplete() {
        boolean complete = levelCount == 1 && steamCount == 1;
        for (int i = 0; i < PUMPS; i++) {
            complete = complete && pumpStateCount[i] == 1 && flowCount[i] == 1;
        }
        return complete;
    }

    /** True when a message is repeated within the transmission or names no pump. */
    boolean aberrant() {
        boolean repeatedData = levelCount > 1 || steamCount > 1;
        for (int i = 0; i < PUMPS; i++) {
            repeatedData = repeatedData || pumpStateCount[i] > 1 || flowCount[i] > 1;
        }
        return duplicate || badPumpNumber || repeatedData;
    }

    boolean stopReceived() { return stopReceived; }
    boolean steamBoilerWaiting() { return steamBoilerWaiting; }
    boolean physicalUnitsReady() { return physicalUnitsReady; }
    boolean levelPresent() { return levelCount > 0; }
    @RoboChartType("real") double levelValue() { return levelValue; }
    boolean steamPresent() { return steamCount > 0; }
    @RoboChartType("real") double steamValue() { return steamValue; }
    boolean pumpStatePresent(int i) { return pumpStateCount[i] > 0; }
    boolean pumpRunning(int i) { return pumpRunning[i]; }
    boolean flowPresent(int i) { return flowCount[i] > 0; }
    boolean flowing(int i) { return flowing[i]; }
    boolean pumpRepaired(int i) { return pumpRepaired[i]; }
    boolean pumpControlRepaired(int i) { return pumpControlRepaired[i]; }
    boolean pumpFailureAcknowledged(int i) { return pumpFailureAcknowledged[i]; }
    boolean pumpControlFailureAcknowledged(int i) { return pumpControlFailureAcknowledged[i]; }
    boolean levelRepaired() { return levelRepaired; }
    boolean steamRepaired() { return steamRepaired; }
    boolean levelFailureAcknowledged() { return levelFailureAcknowledged; }
    boolean steamFailureAcknowledged() { return steamFailureAcknowledged; }
}
