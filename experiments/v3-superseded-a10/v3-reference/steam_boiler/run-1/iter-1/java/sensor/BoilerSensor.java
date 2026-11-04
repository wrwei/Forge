package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.SteamBoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.InputEvent;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;
import steam_boiler.event.WaterFlow;

/**
 * The program's view of the physical units, rebuilt from each cycle's transmission.
 *
 * <p>Per cycle: {@link #startCycle} with the messages the program sent in the previous cycle,
 * {@link #receive} for every incoming message, then {@link #analyse}. The analysis judges the
 * transmission, the pumps, the pump controllers and the two measuring devices, keeps each
 * unit's status, and advances the worst-case estimate of the water level. The controller reads
 * the results through the query methods.
 */
public final class BoilerSensor {

    private final List<PumpMonitor> pumps = new ArrayList<>();
    private final UnitStatus levelDevice = new UnitStatus();
    private final UnitStatus steamDevice = new UnitStatus();

    // What the program sent in the previous cycle.
    private ModeValue lastModeSent = ModeValue.MODE_INITIALIZATION;
    private boolean programReadySent = false;
    private boolean valveOpen = false;

    // Messages of the current transmission.
    @RoboChartType("nat") private int stopMessages = 0;
    @RoboChartType("nat") private int waitingMessages = 0;
    @RoboChartType("nat") private int unitsReadyMessages = 0;
    @RoboChartType("nat") private int levelMessages = 0;
    @RoboChartType("nat") private int steamMessages = 0;
    @RoboChartType("real") private double levelValue = 0.0;
    @RoboChartType("real") private double steamValue = 0.0;
    private boolean levelRepairReported = false;
    private boolean steamRepairReported = false;
    private boolean levelAckReported = false;
    private boolean steamAckReported = false;
    private boolean aberrantMessage = false;

    // Derived quantities read by the controller.
    @RoboChartType("nat") private int successiveStopCount = 0;
    private boolean transmissionFailure = false;
    private boolean steamBoilerWaitingReceived = false;
    private boolean physicalUnitsReadyReceived = false;
    @RoboChartType("real") private double levelReading = 0.0;
    @RoboChartType("real") private double steamReading = 0.0;
    @RoboChartType("real") private double pumpThroughput = 0.0;
    @RoboChartType("real") private double levelEstimateLow = 0.0;
    @RoboChartType("real") private double levelEstimateHigh = 0.0;
    @RoboChartType("real") private double estimatedLevel = 0.0;
    @RoboChartType("real") private double steamEstimateLow = 0.0;
    @RoboChartType("real") private double steamEstimateHigh = 0.0;
    private boolean levelHistory = false;
    private boolean steamHistory = false;
    @RoboChartType("real") private double minimumReachableLevel = 0.0;
    @RoboChartType("real") private double maximumReachableLevel = 0.0;
    private boolean levelDeviceOutOfOrder = false;
    private boolean steamDeviceOutOfOrder = false;
    private boolean pumpControllerOutOfOrder = false;
    private boolean otherUnitOutOfOrder = false;
    @RoboChartType("nat") private int openablePumpCount = 0;
    @RoboChartType("nat") private int closablePumpCount = 0;
    @RoboChartType("nat") private int pumpToOpen = 0;
    @RoboChartType("nat") private int pumpToClose = 0;

    /** Creates the view of a plant with all units sound and nothing received yet. */
    public BoilerSensor() {
        for (int number = 1; number <= SteamBoilerConstants.PUMP_COUNT; number++) {
            pumps.add(new PumpMonitor(number));
        }
        refreshSummary();
    }

    /** Begins a cycle, given the messages the program sent in the previous one. */
    public void startCycle(List<OutputEvent> previouslySent) {
        for (PumpMonitor pump : pumps) {
            pump.startCycle();
        }
        programReadySent = false;
        for (OutputEvent message : previouslySent) {
            if (message instanceof OutputEvent.Mode) {
                OutputEvent.Mode modeMessage = (OutputEvent.Mode) message;
                lastModeSent = modeMessage.value();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            } else if (message instanceof OutputEvent.Valve) {
                valveOpen = !valveOpen;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump open = (OutputEvent.OpenPump) message;
                if (validPump(open.pumpNumber())) {
                    pumps.get(open.pumpNumber() - 1).ordered(true);
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump close = (OutputEvent.ClosePump) message;
                if (validPump(close.pumpNumber())) {
                    pumps.get(close.pumpNumber() - 1).ordered(false);
                }
            }
        }
        stopMessages = 0;
        waitingMessages = 0;
        unitsReadyMessages = 0;
        levelMessages = 0;
        steamMessages = 0;
        levelRepairReported = false;
        steamRepairReported = false;
        levelAckReported = false;
        steamAckReported = false;
        aberrantMessage = false;
    }

    /** Takes one message of the current transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopMessages = stopMessages + 1;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingMessages = waitingMessages + 1;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyMessages = unitsReadyMessages + 1;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            int number = state.report().reportedPump();
            if (validPump(number)) {
                pumps.get(number - 1).reportState(state.report().switchedOn());
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState control = (InputEvent.PumpControlState) message;
            int number = control.report().controlledPump();
            if (validPump(number)) {
                pumps.get(number - 1).reportControl(control.report().flow() == WaterFlow.FLOWING);
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            levelValue = level.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamValue = steam.value();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            if (validPump(repaired.pumpNumber())) {
                pumps.get(repaired.pumpNumber() - 1).reportPumpRepaired();
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            if (validPump(repaired.pumpNumber())) {
                pumps.get(repaired.pumpNumber() - 1).reportControlRepaired();
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairReported = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairReported = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack =
                    (InputEvent.PumpFailureAcknowledgement) message;
            if (validPump(ack.pumpNumber())) {
                pumps.get(ack.pumpNumber() - 1).reportPumpAcknowledged();
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            if (validPump(ack.pumpNumber())) {
                pumps.get(ack.pumpNumber() - 1).reportControlAcknowledged();
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAckReported = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAckReported = true;
        }
    }

    /** Analyses the current transmission once all its messages are received. */
    public void analyse() {
        transmissionFailure = aberrant() || !complete();
        if (stopMessages > 0) {
            if (successiveStopCount < SteamBoilerConstants.STOP_LIMIT) {
                successiveStopCount = successiveStopCount + 1;
            }
        } else {
            successiveStopCount = 0;
        }
        if (waitingMessages > 0) {
            steamBoilerWaitingReceived = true;
        }
        physicalUnitsReadyReceived = unitsReadyMessages > 0;
        if (transmissionFailure) {
            // The readings cannot be trusted; the controller stops the program.
            refreshSummary();
            return;
        }
        levelReading = levelValue;
        steamReading = steamValue;
        if (!steamBoilerWaitingReceived) {
            // Nothing is judged until the plant announces it is waiting.
            for (PumpMonitor pump : pumps) {
                pump.track();
            }
            refreshSummary();
            return;
        }
        for (PumpMonitor pump : pumps) {
            pump.judge();
        }
        if (levelAckReported) {
            levelDevice.acknowledgeDetection();
        }
        if (steamAckReported) {
            steamDevice.acknowledgeDetection();
        }
        if (levelRepairReported) {
            levelDevice.repair();
        }
        if (steamRepairReported) {
            steamDevice.repair();
        }

        // Water that entered and steam that left during the cycle that has just ended.
        double pastInLow = 0.0;
        double pastInHigh = 0.0;
        for (PumpMonitor pump : pumps) {
            pastInLow = pastInLow + pump.pastFlowLow() * SteamBoilerConstants.CYCLE_SECONDS;
            pastInHigh = pastInHigh + pump.pastFlowHigh() * SteamBoilerConstants.CYCLE_SECONDS;
        }
        double previousSteamLow = steamEstimateLow;
        double previousSteamHigh = steamEstimateHigh;
        if (!steamHistory) {
            previousSteamLow = 0.0;
            previousSteamHigh = SteamBoilerConstants.MAX_STEAM_W;
        }
        double pastOutLow = leastSteamOut(previousSteamLow);
        double pastOutHigh = mostSteamOut(previousSteamHigh);

        // Steam measuring device: static range [0, W] and gradients U1, U2.
        double predictedSteamLow = Math.max(0.0,
                previousSteamLow - SteamBoilerConstants.STEAM_DECREASE_U2
                        * SteamBoilerConstants.CYCLE_SECONDS);
        double predictedSteamHigh = Math.min(SteamBoilerConstants.MAX_STEAM_W,
                previousSteamHigh + SteamBoilerConstants.STEAM_INCREASE_U1
                        * SteamBoilerConstants.CYCLE_SECONDS);
        if (!steamDevice.outOfOrder()) {
            boolean inRange = steamReading >= 0.0
                    && steamReading <= SteamBoilerConstants.MAX_STEAM_W;
            boolean reachable = steamReading >= predictedSteamLow
                    && steamReading <= predictedSteamHigh;
            if (!inRange || !reachable) {
                steamDevice.detectFailure();
            }
        }
        if (steamDevice.outOfOrder()) {
            steamEstimateLow = predictedSteamLow;
            steamEstimateHigh = predictedSteamHigh;
        } else {
            steamEstimateLow = steamReading;
            steamEstimateHigh = steamReading;
        }
        steamHistory = true;

        // Water-level measuring device: static range [0, C] and the dynamics of the cycle.
        double predictedLow = levelEstimateLow + pastInLow - pastOutHigh;
        double predictedHigh = levelEstimateHigh + pastInHigh - pastOutLow;
        if (valveOpen) {
            // The evacuation valve drains at a rate the requirements do not give.
            predictedLow = 0.0;
        }
        if (!levelHistory) {
            predictedLow = 0.0;
            predictedHigh = SteamBoilerConstants.CAPACITY_C;
        }
        predictedLow = clampLevel(predictedLow);
        predictedHigh = clampLevel(predictedHigh);
        if (!levelDevice.outOfOrder()) {
            boolean inRange = levelReading >= 0.0
                    && levelReading <= SteamBoilerConstants.CAPACITY_C;
            boolean reachable = levelReading >= predictedLow && levelReading <= predictedHigh;
            if (!inRange || !reachable) {
                levelDevice.detectFailure();
            }
        }
        if (levelDevice.outOfOrder()) {
            levelEstimateLow = predictedLow;
            levelEstimateHigh = predictedHigh;
        } else {
            levelEstimateLow = levelReading;
            levelEstimateHigh = levelReading;
        }
        levelHistory = true;

        // Worst case over the coming cycle; a pump ordered on now pours nothing during it.
        double nextInLow = 0.0;
        double nextInHigh = 0.0;
        for (PumpMonitor pump : pumps) {
            nextInLow = nextInLow + pump.nextFlowLow() * SteamBoilerConstants.CYCLE_SECONDS;
            nextInHigh = nextInHigh + pump.nextFlowHigh() * SteamBoilerConstants.CYCLE_SECONDS;
        }
        minimumReachableLevel = levelEstimateLow + nextInLow - mostSteamOut(steamEstimateHigh);
        maximumReachableLevel = levelEstimateHigh + nextInHigh - leastSteamOut(steamEstimateLow);
        refreshSummary();
    }

    /** Failure announcements and repair acknowledgements due in the current cycle. */
    public List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (PumpMonitor pump : pumps) {
            pump.addProtocolMessages(messages);
        }
        if (levelDevice.detectionPending()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamDevice.detectionPending()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelDevice.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamDevice.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return messages;
    }

    /** Number of successive transmissions, up to the current one, that carried STOP. */
    @RoboChartType("nat")
    public int successiveStopCount() {
        return successiveStopCount;
    }

    /** Whether the current transmission lacks an indispensable message or has an aberrant one. */
    public boolean transmissionFailure() {
        return transmissionFailure;
    }

    /** Whether STEAM_BOILER_WAITING has arrived in this or an earlier transmission. */
    public boolean steamBoilerWaitingReceived() {
        return steamBoilerWaitingReceived;
    }

    /** Whether the current transmission carries PHYSICAL_UNITS_READY. */
    public boolean physicalUnitsReadyReceived() {
        return physicalUnitsReadyReceived;
    }

    /** q: the water level most recently reported (litre). */
    @RoboChartType("real")
    public double levelReading() {
        return levelReading;
    }

    /** v: the steam rate most recently reported (litre/sec). */
    @RoboChartType("real")
    public double steamReading() {
        return steamReading;
    }

    /** p: the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return pumpThroughput;
    }

    /** Lower bound of the water level the program currently assumes (litre). */
    @RoboChartType("real")
    public double levelEstimateLow() {
        return levelEstimateLow;
    }

    /** Upper bound of the water level the program currently assumes (litre). */
    @RoboChartType("real")
    public double levelEstimateHigh() {
        return levelEstimateHigh;
    }

    /** Midpoint of the level estimate, the level used for regulation (litre). */
    @RoboChartType("real")
    public double estimatedLevel() {
        return estimatedLevel;
    }

    /** Lowest level the boiler can reach by the end of the coming cycle (litre). */
    @RoboChartType("real")
    public double minimumReachableLevel() {
        return minimumReachableLevel;
    }

    /** Highest level the boiler can reach by the end of the coming cycle (litre). */
    @RoboChartType("real")
    public double maximumReachableLevel() {
        return maximumReachableLevel;
    }

    /** Whether the water-level measuring device is out of order. */
    public boolean levelDeviceOutOfOrder() {
        return levelDeviceOutOfOrder;
    }

    /** Whether the steam measuring device is out of order. */
    public boolean steamDeviceOutOfOrder() {
        return steamDeviceOutOfOrder;
    }

    /** Whether some pump controller is out of order. */
    public boolean pumpControllerOutOfOrder() {
        return pumpControllerOutOfOrder;
    }

    /** Whether some unit other than the water-level measuring device is out of order. */
    public boolean otherUnitOutOfOrder() {
        return otherUnitOutOfOrder;
    }

    /** Number of sound pumps that are off and can be switched on. */
    @RoboChartType("nat")
    public int openablePumpCount() {
        return openablePumpCount;
    }

    /** Number of sound pumps that are on and can be switched off. */
    @RoboChartType("nat")
    public int closablePumpCount() {
        return closablePumpCount;
    }

    /** The sound pump to switch on next (0 when there is none). */
    @RoboChartType("nat")
    public int pumpToOpen() {
        return pumpToOpen;
    }

    /** The sound pump to switch off next (0 when there is none). */
    @RoboChartType("nat")
    public int pumpToClose() {
        return pumpToClose;
    }

    private boolean complete() {
        boolean complete = levelMessages == 1 && steamMessages == 1;
        for (PumpMonitor pump : pumps) {
            if (!pump.transmissionComplete()) {
                complete = false;
            }
        }
        return complete;
    }

    private boolean aberrant() {
        boolean aberrant = aberrantMessage;
        for (PumpMonitor pump : pumps) {
            if (pump.aberrant()) {
                aberrant = true;
            }
        }
        boolean initializing = lastModeSent == ModeValue.MODE_INITIALIZATION;
        if (waitingMessages > 0 && !initializing) {
            aberrant = true;
        }
        if (unitsReadyMessages > 0 && !(initializing && programReadySent)) {
            aberrant = true;
        }
        if (levelRepairReported && !levelDevice.outOfOrder()) {
            aberrant = true;
        }
        if (steamRepairReported && !steamDevice.outOfOrder()) {
            aberrant = true;
        }
        if (levelAckReported && !levelDevice.detectionPending()) {
            aberrant = true;
        }
        if (steamAckReported && !steamDevice.detectionPending()) {
            aberrant = true;
        }
        return aberrant;
    }

    private void refreshSummary() {
        pumpThroughput = 0.0;
        openablePumpCount = 0;
        closablePumpCount = 0;
        pumpToOpen = 0;
        pumpToClose = 0;
        boolean pumpOutOfOrder = false;
        boolean controlOutOfOrder = false;
        for (PumpMonitor pump : pumps) {
            if (pump.believedPouring()) {
                pumpThroughput = pumpThroughput + SteamBoilerConstants.PUMP_CAPACITY_P;
            }
            if (pump.openable()) {
                openablePumpCount = openablePumpCount + 1;
                if (pumpToOpen == 0) {
                    pumpToOpen = pump.pumpNumber();
                }
            }
            if (pump.closable()) {
                closablePumpCount = closablePumpCount + 1;
                pumpToClose = pump.pumpNumber();
            }
            if (pump.pumpOutOfOrder()) {
                pumpOutOfOrder = true;
            }
            if (pump.controlOutOfOrder()) {
                controlOutOfOrder = true;
            }
        }
        levelDeviceOutOfOrder = levelDevice.outOfOrder();
        steamDeviceOutOfOrder = steamDevice.outOfOrder();
        pumpControllerOutOfOrder = controlOutOfOrder;
        otherUnitOutOfOrder = steamDeviceOutOfOrder || pumpOutOfOrder || controlOutOfOrder;
        estimatedLevel = (levelEstimateLow + levelEstimateHigh) / 2.0;
    }

    private static boolean validPump(@RoboChartType("nat") int number) {
        return number >= 1 && number <= SteamBoilerConstants.PUMP_COUNT;
    }

    @RoboChartType("real")
    private static double clampLevel(@RoboChartType("real") double level) {
        return Math.max(0.0, Math.min(SteamBoilerConstants.CAPACITY_C, level));
    }

    /** Least steam (litre) that can leave in one cycle when the rate starts at {@code rate}. */
    @RoboChartType("real")
    private static double leastSteamOut(@RoboChartType("real") double rate) {
        double t = SteamBoilerConstants.CYCLE_SECONDS;
        double u2 = SteamBoilerConstants.STEAM_DECREASE_U2;
        if (rate >= u2 * t) {
            return rate * t - u2 * t * t / 2.0;
        }
        return rate * rate / (2.0 * u2);
    }

    /** Most steam (litre) that can leave in one cycle when the rate starts at {@code rate}. */
    @RoboChartType("real")
    private static double mostSteamOut(@RoboChartType("real") double rate) {
        double t = SteamBoilerConstants.CYCLE_SECONDS;
        double u1 = SteamBoilerConstants.STEAM_INCREASE_U1;
        double w = SteamBoilerConstants.MAX_STEAM_W;
        if (rate + u1 * t <= w) {
            return rate * t + u1 * t * t / 2.0;
        }
        double untilMax = Math.max(0.0, (w - rate) / u1);
        return rate * untilMax + u1 * untilMax * untilMax / 2.0 + w * (t - untilMax);
    }
}
