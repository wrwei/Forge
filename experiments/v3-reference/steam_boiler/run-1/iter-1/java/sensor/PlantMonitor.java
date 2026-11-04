package steam_boiler.sensor;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.WaterFlow;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * Receives the messages of each incoming transmission and analyses them:
 * counts successive STOP messages, judges transmission, pump, pump-controller,
 * level-device and steam-device failures, keeps the soundness and
 * acknowledgement status of every unit, maintains the level estimate, and
 * publishes the results to the {@link BoilerSensor}. It also emits the
 * failure announcements (repeated until acknowledged) and the repair
 * acknowledgements of the cycle.
 */
public final class PlantMonitor {

    private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    private static final double CYCLE = BoilerConstants.CYCLE_SECONDS;
    private static final int FLOW_CHECK_DELAY = 2;

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    // Messages of the transmission being received.
    private boolean stopReceived;
    private boolean waitingReceived;
    private boolean readyReceived;
    private boolean aberrantReceived;
    private int levelMessages;
    private int steamMessages;
    private double levelValue;
    private double steamValue;
    private final int[] pumpStateMessages = new int[PUMPS];
    private final boolean[] pumpOn = new boolean[PUMPS];
    private final int[] controlStateMessages = new int[PUMPS];
    private final boolean[] waterFlowing = new boolean[PUMPS];
    private final boolean[] pumpRepairReceived = new boolean[PUMPS];
    private final boolean[] controlRepairReceived = new boolean[PUMPS];
    private boolean levelRepairReceived;
    private boolean steamRepairReceived;
    private final boolean[] pumpAckReceived = new boolean[PUMPS];
    private final boolean[] controlAckReceived = new boolean[PUMPS];
    private boolean levelAckReceived;
    private boolean steamAckReceived;

    // Soundness of each unit and acknowledgement of its failure announcement.
    private final boolean[] pumpOutOfOrder = new boolean[PUMPS];
    private final boolean[] pumpFailureAcknowledged = new boolean[PUMPS];
    private final boolean[] controlOutOfOrder = new boolean[PUMPS];
    private final boolean[] controlFailureAcknowledged = new boolean[PUMPS];
    private boolean levelOutOfOrder;
    private boolean levelFailureAcknowledged;
    private boolean steamOutOfOrder;
    private boolean steamFailureAcknowledged;

    // Successive transmissions carrying STOP.
    private int stopCount;

    // What the previous analysed transmission established.
    private boolean historyKnown;
    private double previousLow;
    private double previousHigh;
    private double previousSteam;
    private boolean previousSteamValid;
    private final boolean[] previousMayPour = new boolean[PUMPS];
    private final boolean[] previousPouring = new boolean[PUMPS];

    // Expected pump positions and controller observations.
    private final boolean[] expectedOn = new boolean[PUMPS];
    private final boolean[] expectedFlow = new boolean[PUMPS];
    private final int[] flowCountdown = new int[PUMPS];

    // Lower and upper bound on the water level.
    private double estimateLow;
    private double estimateHigh;

    /** Creates a monitor that publishes to the given sensor and reads the orders of the given actuator. */
    public PlantMonitor(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** Records one message of the transmission being received. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            readyReceived = true;
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            levelValue = level.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamValue = steam.value();
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            int index = state.reading().pumpIndex() - 1;
            if (validPump(index)) {
                pumpStateMessages[index] = pumpStateMessages[index] + 1;
                pumpOn[index] = state.reading().position() == PumpPosition.PUMP_ON;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState state = (InputEvent.PumpControlState) message;
            int index = state.reading().controllerIndex() - 1;
            if (validPump(index)) {
                controlStateMessages[index] = controlStateMessages[index] + 1;
                waterFlowing[index] = state.reading().observation() == WaterFlow.FLOW;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            int index = repaired.pump() - 1;
            if (validPump(index)) {
                pumpRepairReceived[index] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            int index = repaired.pump() - 1;
            if (validPump(index)) {
                controlRepairReceived[index] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairReceived = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            int index = ack.pump() - 1;
            if (validPump(index)) {
                pumpAckReceived[index] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            int index = ack.pump() - 1;
            if (validPump(index)) {
                controlAckReceived[index] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAckReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAckReceived = true;
        }
    }

    /**
     * Analyses the transmission received since the last call, publishes the
     * results to the sensor and emits this cycle's failure announcements and
     * repair acknowledgements.
     */
    public void analyse() {
        updateStopCount();
        boolean failed = aberrantReceived || !transmissionComplete()
                || unexpectedAcknowledgement() || unexpectedRepair();
        sensor.stopCount = stopCount;
        sensor.transmissionFailed = failed;
        sensor.waitingReceived = waitingReceived;
        sensor.unitsReadyReceived = readyReceived;
        if (!failed) {
            boolean levelJustRepaired = levelRepairReceived;
            boolean steamJustRepaired = steamRepairReceived;
            acceptAcknowledgements();
            if (historyKnown) {
                applyOrders();
            } else {
                initialiseExpectations();
            }
            acceptRepairs();
            if (historyKnown) {
                detectPumpFailures();
                detectPumpControlFailures();
            }
            detectLevelFailure(levelJustRepaired);
            detectSteamFailure(steamJustRepaired);
            updateEstimate();
            publish();
            announceFailures();
            remember();
        }
        clearReception();
    }

    private void updateStopCount() {
        if (stopReceived) {
            if (stopCount < BoilerConstants.STOP_LIMIT) {
                stopCount = stopCount + 1;
            }
        } else {
            stopCount = 0;
        }
    }

    private boolean transmissionComplete() {
        if (levelMessages != 1 || steamMessages != 1) {
            return false;
        }
        for (int index = 0; index < PUMPS; index++) {
            if (pumpStateMessages[index] != 1 || controlStateMessages[index] != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean unexpectedAcknowledgement() {
        for (int index = 0; index < PUMPS; index++) {
            if (pumpAckReceived[index] && !awaitingAcknowledgement(pumpOutOfOrder[index], pumpFailureAcknowledged[index])) {
                return true;
            }
            if (controlAckReceived[index]
                    && !awaitingAcknowledgement(controlOutOfOrder[index], controlFailureAcknowledged[index])) {
                return true;
            }
        }
        if (levelAckReceived && !awaitingAcknowledgement(levelOutOfOrder, levelFailureAcknowledged)) {
            return true;
        }
        return steamAckReceived && !awaitingAcknowledgement(steamOutOfOrder, steamFailureAcknowledged);
    }

    private static boolean awaitingAcknowledgement(boolean outOfOrder, boolean acknowledged) {
        return outOfOrder && !acknowledged;
    }

    private boolean unexpectedRepair() {
        for (int index = 0; index < PUMPS; index++) {
            if (pumpRepairReceived[index] && !pumpOutOfOrder[index]) {
                return true;
            }
            if (controlRepairReceived[index] && !controlOutOfOrder[index]) {
                return true;
            }
        }
        if (levelRepairReceived && !levelOutOfOrder) {
            return true;
        }
        return steamRepairReceived && !steamOutOfOrder;
    }

    private void acceptAcknowledgements() {
        for (int index = 0; index < PUMPS; index++) {
            if (pumpAckReceived[index]) {
                pumpFailureAcknowledged[index] = true;
            }
            if (controlAckReceived[index]) {
                controlFailureAcknowledged[index] = true;
            }
        }
        if (levelAckReceived) {
            levelFailureAcknowledged = true;
        }
        if (steamAckReceived) {
            steamFailureAcknowledged = true;
        }
    }

    // A start or stop order changes the position the pump must indicate at the
    // next transmission, and the observation its controller must indicate at
    // the second transmission (the pump pours only after a cycle of pressure
    // balancing).
    private void applyOrders() {
        for (int index = 0; index < PUMPS; index++) {
            int pump = index + 1;
            if (actuator.openOrdered(pump) && !expectedOn[index]) {
                expectedOn[index] = true;
                expectedFlow[index] = true;
                flowCountdown[index] = FLOW_CHECK_DELAY;
            } else if (actuator.closeOrdered(pump) && expectedOn[index]) {
                expectedOn[index] = false;
                expectedFlow[index] = false;
                flowCountdown[index] = FLOW_CHECK_DELAY;
            }
        }
    }

    private void initialiseExpectations() {
        for (int index = 0; index < PUMPS; index++) {
            expectedOn[index] = pumpOn[index];
            expectedFlow[index] = waterFlowing[index];
            flowCountdown[index] = 0;
        }
    }

    private void acceptRepairs() {
        for (int index = 0; index < PUMPS; index++) {
            if (pumpRepairReceived[index]) {
                pumpOutOfOrder[index] = false;
                pumpFailureAcknowledged[index] = false;
                expectedOn[index] = pumpOn[index];
                expectedFlow[index] = pumpOn[index];
                flowCountdown[index] = FLOW_CHECK_DELAY;
                actuator.apply(new OutputEvent.PumpRepairedAcknowledgement(index + 1));
            }
            if (controlRepairReceived[index]) {
                controlOutOfOrder[index] = false;
                controlFailureAcknowledged[index] = false;
                expectedFlow[index] = waterFlowing[index];
                flowCountdown[index] = 0;
                actuator.apply(new OutputEvent.PumpControlRepairedAcknowledgement(index + 1));
            }
        }
        if (levelRepairReceived) {
            levelOutOfOrder = false;
            levelFailureAcknowledged = false;
            actuator.apply(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamRepairReceived) {
            steamOutOfOrder = false;
            steamFailureAcknowledged = false;
            actuator.apply(new OutputEvent.SteamRepairedAcknowledgement());
        }
    }

    // A pump has failed when it does not indicate the position it was ordered
    // to take, or when it changes position without an order.
    private void detectPumpFailures() {
        for (int index = 0; index < PUMPS; index++) {
            if (!pumpOutOfOrder[index] && pumpOn[index] != expectedOn[index]) {
                pumpOutOfOrder[index] = true;
                pumpFailureAcknowledged[index] = false;
            }
        }
    }

    // A pump controller has failed when, with its pump known to work, it does
    // not indicate the coming or ceasing of water flow by the second
    // transmission after an order, or changes its observation without one.
    private void detectPumpControlFailures() {
        for (int index = 0; index < PUMPS; index++) {
            if (flowCountdown[index] > 0) {
                flowCountdown[index] = flowCountdown[index] - 1;
            }
            boolean pumpWorks = !pumpOutOfOrder[index] && pumpOn[index] == expectedOn[index];
            if (!controlOutOfOrder[index] && pumpWorks && flowCountdown[index] == 0
                    && waterFlowing[index] != expectedFlow[index]) {
                controlOutOfOrder[index] = true;
                controlFailureAcknowledged[index] = false;
            }
        }
    }

    private void detectLevelFailure(boolean justRepaired) {
        if (levelOutOfOrder) {
            return;
        }
        boolean outOfRange = levelValue < 0.0 || levelValue > BoilerConstants.CAPACITY_C;
        boolean incompatible = false;
        if (historyKnown && !justRepaired) {
            incompatible = levelValue < lowestReachableLevel() || levelValue > highestReachableLevel();
        }
        if (outOfRange || incompatible) {
            levelOutOfOrder = true;
            levelFailureAcknowledged = false;
        }
    }

    private void detectSteamFailure(boolean justRepaired) {
        if (steamOutOfOrder) {
            return;
        }
        boolean outOfRange = steamValue < 0.0 || steamValue > BoilerConstants.MAX_STEAM_W;
        boolean incompatible = false;
        if (historyKnown && previousSteamValid && !justRepaired) {
            incompatible = steamValue > previousSteam + BoilerConstants.STEAM_RISE_U1 * CYCLE
                    || steamValue < previousSteam - BoilerConstants.STEAM_FALL_U2 * CYCLE;
        }
        if (outOfRange || incompatible) {
            steamOutOfOrder = true;
            steamFailureAcknowledged = false;
        }
    }

    // Bounds on the level reachable since the previous transmission under
    // extreme behaviour: pumps delivering nothing up to every pump that may
    // pour delivering its capacity, steam within its gradients. An open
    // evacuation valve drains at an unspecified rate, so it removes the
    // lower bound.
    private double lowestReachableLevel() {
        if (actuator.valveOpened()) {
            return 0.0;
        }
        return previousLow - steamOutMaximum(previousSteam, previousSteamValid);
    }

    private double highestReachableLevel() {
        return previousHigh + pumpedMaximumSincePrevious() - steamOutMinimum(previousSteam, previousSteamValid);
    }

    // While the level device is sound the estimate is the reading itself;
    // while it is out of order the bounds are advanced from the steam
    // dynamics and the water known to be supplied by the pumps.
    private void updateEstimate() {
        if (!levelOutOfOrder) {
            estimateLow = levelValue;
            estimateHigh = levelValue;
        } else if (historyKnown) {
            estimateLow = Math.max(0.0, previousLow + pumpedKnownSincePrevious()
                    - steamOutMaximum(previousSteam, previousSteamValid));
            if (actuator.valveOpened()) {
                estimateLow = 0.0;
            }
            estimateHigh = Math.min(BoilerConstants.CAPACITY_C, previousHigh + pumpedMaximumSincePrevious()
                    - steamOutMinimum(previousSteam, previousSteamValid));
        } else {
            estimateLow = 0.0;
            estimateHigh = BoilerConstants.CAPACITY_C;
        }
    }

    private void publish() {
        boolean steamValid = !steamOutOfOrder;
        int pouring = 0;
        int knownPouring = 0;
        int mayPour = 0;
        boolean anyPumpOutOfOrder = false;
        boolean anyControlOutOfOrder = false;
        for (int index = 0; index < PUMPS; index++) {
            if (pumpOn[index] && waterFlowing[index]) {
                pouring = pouring + 1;
            }
            if (knownToPour(index)) {
                knownPouring = knownPouring + 1;
            }
            if (pumpOn[index] || pumpOutOfOrder[index]) {
                mayPour = mayPour + 1;
            }
            anyPumpOutOfOrder = anyPumpOutOfOrder || pumpOutOfOrder[index];
            anyControlOutOfOrder = anyControlOutOfOrder || controlOutOfOrder[index];
        }
        double cycleVolume = BoilerConstants.PUMP_CAPACITY_P * CYCLE;
        sensor.levelReading = levelValue;
        sensor.steamReading = steamValue;
        sensor.pumpThroughput = BoilerConstants.PUMP_CAPACITY_P * pouring;
        sensor.levelFailure = levelOutOfOrder;
        sensor.steamFailure = steamOutOfOrder;
        sensor.pumpFailure = anyPumpOutOfOrder;
        sensor.pumpControlFailure = anyControlOutOfOrder;
        sensor.estimatedLevel = (estimateLow + estimateHigh) / 2.0;
        sensor.predictedMinimum = estimateLow + cycleVolume * knownPouring
                - steamOutMaximum(steamValue, steamValid);
        sensor.predictedMaximum = estimateHigh + cycleVolume * mayPour
                - steamOutMinimum(steamValue, steamValid);
        sensor.predictedMaximumWithoutPumps = estimateHigh - steamOutMinimum(steamValue, steamValid);
    }

    private void announceFailures() {
        for (int index = 0; index < PUMPS; index++) {
            if (awaitingAcknowledgement(pumpOutOfOrder[index], pumpFailureAcknowledged[index])) {
                actuator.apply(new OutputEvent.PumpFailureDetection(index + 1));
            }
            if (awaitingAcknowledgement(controlOutOfOrder[index], controlFailureAcknowledged[index])) {
                actuator.apply(new OutputEvent.PumpControlFailureDetection(index + 1));
            }
        }
        if (awaitingAcknowledgement(levelOutOfOrder, levelFailureAcknowledged)) {
            actuator.apply(new OutputEvent.LevelFailureDetection());
        }
        if (awaitingAcknowledgement(steamOutOfOrder, steamFailureAcknowledged)) {
            actuator.apply(new OutputEvent.SteamFailureDetection());
        }
    }

    private void remember() {
        historyKnown = true;
        previousLow = estimateLow;
        previousHigh = estimateHigh;
        previousSteam = steamValue;
        previousSteamValid = !steamOutOfOrder;
        for (int index = 0; index < PUMPS; index++) {
            previousMayPour[index] = pumpOn[index] || pumpOutOfOrder[index];
            previousPouring[index] = knownToPour(index);
        }
    }

    private boolean knownToPour(int index) {
        return pumpOn[index] && waterFlowing[index] && !pumpOutOfOrder[index] && !controlOutOfOrder[index];
    }

    // A pump closed by the last transmission stops at once; a pump opened by
    // it pours nothing during the following cycle.
    private double pumpedMaximumSincePrevious() {
        int count = 0;
        for (int index = 0; index < PUMPS; index++) {
            if (previousMayPour[index] && !actuator.closeOrdered(index + 1)) {
                count = count + 1;
            }
        }
        return BoilerConstants.PUMP_CAPACITY_P * CYCLE * count;
    }

    private double pumpedKnownSincePrevious() {
        int count = 0;
        for (int index = 0; index < PUMPS; index++) {
            if (previousPouring[index] && !actuator.closeOrdered(index + 1)) {
                count = count + 1;
            }
        }
        return BoilerConstants.PUMP_CAPACITY_P * CYCLE * count;
    }

    private static double steamOutMaximum(double rate, boolean valid) {
        double bound = BoilerConstants.MAX_STEAM_W * CYCLE;
        if (!valid) {
            return bound;
        }
        double rising = rate * CYCLE + 0.5 * BoilerConstants.STEAM_RISE_U1 * CYCLE * CYCLE;
        return Math.min(rising, bound);
    }

    private static double steamOutMinimum(double rate, boolean valid) {
        if (!valid) {
            return 0.0;
        }
        double falling = rate * CYCLE - 0.5 * BoilerConstants.STEAM_FALL_U2 * CYCLE * CYCLE;
        return Math.max(falling, 0.0);
    }

    private static boolean validPump(int index) {
        return index >= 0 && index < PUMPS;
    }

    private void clearReception() {
        stopReceived = false;
        waitingReceived = false;
        readyReceived = false;
        aberrantReceived = false;
        levelMessages = 0;
        steamMessages = 0;
        levelRepairReceived = false;
        steamRepairReceived = false;
        levelAckReceived = false;
        steamAckReceived = false;
        for (int index = 0; index < PUMPS; index++) {
            pumpStateMessages[index] = 0;
            controlStateMessages[index] = 0;
            pumpRepairReceived[index] = false;
            controlRepairReceived[index] = false;
            pumpAckReceived[index] = false;
            controlAckReceived[index] = false;
        }
    }
}
