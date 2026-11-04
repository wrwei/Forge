package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.data.PumpControlReading;
import steam_boiler.data.PumpPosition;
import steam_boiler.data.PumpStateReading;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;

/**
 * The receiving side of the message transmission system and the program's analysis of the
 * information it carries (SB-V3). Each cycle {@link #receive(List)} takes the whole incoming
 * transmission at once; the sensor then judges the transmission (SB-FR5), counts successive
 * STOP messages (SB-V10), processes acknowledgements and repairs (SB-FR7), detects unit
 * failures (SB-FR1 to SB-FR4), keeps the per-unit status (SB-V9), derives the pump throughput
 * p (SB-V7), maintains the level estimate (SB-V8) and forecasts the worst-case level one cycle
 * ahead (SB-V4, SB-Beh25). The controller reads the results through the query methods.
 */
public final class BoilerSensor {

    @RoboChartType("real") private static final double CYCLE = SteamBoilerConstants.CYCLE_SECONDS;
    @RoboChartType("nat") private static final int PUMPS = SteamBoilerConstants.PUMP_COUNT;

    private final UnitStatusTable units = new UnitStatusTable();
    private final PumpTracker[] pumps = new PumpTracker[PUMPS];

    // ---- contents of the current transmission ----
    private boolean stopInTransmission = false;
    private boolean boilerWaitingInTransmission = false;
    private boolean unitsReadyInTransmission = false;
    private boolean aberrantMessage = false;
    @RoboChartType("nat") private int levelMessages = 0;
    @RoboChartType("nat") private int steamMessages = 0;
    private final int[] pumpStateMessages = new int[PUMPS];
    private final int[] pumpControlMessages = new int[PUMPS];
    private final List<Integer> pendingAcknowledgements = new ArrayList<>();
    private final List<Integer> pendingRepairs = new ArrayList<>();
    private final List<OutputEvent> repairAcknowledgements = new ArrayList<>();

    // ---- current measures and derived quantities ----
    /** q: the water level most recently reported by the level measuring device (SB-V5). */
    @RoboChartType("real") private double waterLevel = 0.0;
    /** v: the steam rate most recently reported by the steam measuring device (SB-V6). */
    @RoboChartType("real") private double steamRate = 0.0;
    /** p: the current throughput of the pumps taken together (SB-V7). */
    @RoboChartType("real") private double pumpThroughput = 0.0;
    /** Lower bound of the level estimate (SB-V8); equals q while the level device is sound. */
    @RoboChartType("real") private double levelLowerBound = 0.0;
    /** Upper bound of the level estimate (SB-V8); equals q while the level device is sound. */
    @RoboChartType("real") private double levelUpperBound = 0.0;
    @RoboChartType("real") private double levelEstimate = 0.0;
    @RoboChartType("real") private double minimumLevelForecast = 0.0;
    @RoboChartType("real") private double maximumLevelForecast = 0.0;
    /** Number of successive transmissions that have carried STOP (SB-V10). */
    @RoboChartType("nat") private int successiveStopCount = 0;
    private boolean transmissionFailed = false;
    private boolean stopDemanded = false;
    private boolean steamRateNonZero = false;
    private boolean levelBelowNormal = false;
    private boolean levelAboveNormal = false;
    private boolean levelLimitRisk = false;
    private boolean levelDeviceOutOfOrder = false;
    private boolean steamOrPumpControllerOutOfOrder = false;
    private boolean nonLevelUnitOutOfOrder = false;
    @RoboChartType("nat") private int surePouring = 0;
    @RoboChartType("nat") private int possiblePouring = 0;
    @RoboChartType("nat") private int stuckPouring = 0;

    // ---- memory of earlier cycles ----
    /** STEAM_BOILER_WAITING has arrived in this or an earlier transmission (SB-Beh2). */
    private boolean plantWaitingSeen = false;
    /** The program has sent PROGRAM_READY in an earlier transmission (SB-Beh7). */
    private boolean programReadySent = false;
    /** The mode carried by the program's last MODE(m) message (SB-Beh24). */
    private OperatingMode lastReportedMode = OperatingMode.INITIALIZATION_MODE;
    @RoboChartType("real") private double previousSteamRate = 0.0;
    private boolean previousSteamValid = false;
    private boolean previousLevelValid = false;
    @RoboChartType("real") private double previousLowerBound = 0.0;
    @RoboChartType("real") private double previousUpperBound = 0.0;
    private boolean estimateValid = false;
    @RoboChartType("nat") private int previousSurePouring = 0;
    @RoboChartType("nat") private int previousPossiblePouring = 0;

    /** Creates the sensor with every unit believed sound and no transmission received yet. */
    public BoilerSensor() {
        for (int index = 0; index < PUMPS; index++) {
            pumps[index] = new PumpTracker();
        }
    }

    // =====================================================================================
    // Receive stage
    // =====================================================================================

    /**
     * Receives the whole incoming transmission of one cycle; all its messages are considered
     * received simultaneously (SB-V3). Then analyses it, unless the transmission has failed.
     */
    public void receive(List<InputEvent> messages) {
        startTransmission();
        for (InputEvent message : messages) {
            sort(message);
        }
        if (stopInTransmission) {
            successiveStopCount = successiveStopCount + 1;
        } else {
            successiveStopCount = 0;
        }
        if (boilerWaitingInTransmission) {
            plantWaitingSeen = true;
        }
        transmissionFailed = aberrantMessage || startupMessageAberrant() || !dataComplete();
        if (!transmissionFailed) {
            analyse();
        }
        judge();
    }

    /**
     * Records what the program sent in the cycle's outgoing transmission: the pump orders, so
     * that the next transmissions can be checked against them (SB-FR1, SB-FR2), and the mode
     * and PROGRAM_READY messages, which decide whether a start-up message is aberrant (SB-FR5).
     */
    public void recordOrders(List<OutputEvent> transmission) {
        for (OutputEvent message : transmission) {
            if (message instanceof OutputEvent.Mode) {
                OutputEvent.Mode mode = (OutputEvent.Mode) message;
                lastReportedMode = mode.modeValue();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump open = (OutputEvent.OpenPump) message;
                orderPump(open.pump(), PumpPosition.PUMP_ON);
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump close = (OutputEvent.ClosePump) message;
                orderPump(close.pump(), PumpPosition.PUMP_OFF);
            }
        }
    }

    /**
     * The failure-protocol messages of this cycle: a failure detection for every unit whose
     * failure is not yet acknowledged (SB-FR6), and an acknowledgement for every repair
     * reported in this transmission (SB-FR7).
     */
    public List<OutputEvent> unitProtocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int pump = 1; pump <= PUMPS; pump++) {
            if (units.awaitingAcknowledgement(UnitStatusTable.pumpUnit(pump))) {
                messages.add(new OutputEvent.PumpFailureDetection(pump));
            }
        }
        for (int pump = 1; pump <= PUMPS; pump++) {
            if (units.awaitingAcknowledgement(UnitStatusTable.pumpControllerUnit(pump))) {
                messages.add(new OutputEvent.PumpControlFailureDetection(pump));
            }
        }
        if (units.awaitingAcknowledgement(UnitStatusTable.LEVEL_UNIT)) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (units.awaitingAcknowledgement(UnitStatusTable.STEAM_UNIT)) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        messages.addAll(repairAcknowledgements);
        return messages;
    }

    // =====================================================================================
    // Queries used by the controller: judgements of the current cycle
    // =====================================================================================

    /** True if STOP has arrived on three successive transmissions, this one included (SB-Beh23, SB-V10). */
    public boolean stopDemanded() {
        return stopDemanded;
    }

    /**
     * True if this transmission lacks an indispensable message or carries an aberrant one
     * (SB-FR5, SB-Beh9, SB-Beh14, SB-Beh18, SB-Beh21).
     */
    public boolean transmissionFailed() {
        return transmissionFailed;
    }

    /** True once STEAM_BOILER_WAITING has arrived, in this or an earlier transmission (SB-Beh2). */
    public boolean steamBoilerWaitingReceived() {
        return plantWaitingSeen;
    }

    /** True if this transmission carries PHYSICAL_UNITS_READY (SB-Beh8). */
    public boolean physicalUnitsReadyReceived() {
        return unitsReadyInTransmission;
    }

    /** True if the steam rate v reported in this transmission is not zero (SB-Beh3, SB-V6). */
    public boolean steamRateNonZero() {
        return steamRateNonZero;
    }

    /**
     * True if the level the program regulates on is below N1. That level is q while the level
     * device is sound and the midpoint of the estimated bounds otherwise (SB-V5, SB-V8,
     * SB-Beh5, SB-Beh10, SB-Beh19).
     */
    public boolean levelBelowNormal() {
        return levelBelowNormal;
    }

    /** True if the level the program regulates on is above N2 (SB-Beh4, SB-Beh10, SB-Beh19). */
    public boolean levelAboveNormal() {
        return levelAboveNormal;
    }

    /**
     * True if the level risks reaching M1 or M2 within the next cycle under worst-case
     * behaviour: maximal steam (bounded by W and U1) with only pumps already pouring, since a
     * pump ordered on pours nothing during its five-second pressure balancing; or minimal steam
     * (bounded by U2) with every sound pump closed, which takes effect at once, and every failed
     * pump that may be pouring still pouring (SB-V4, SB-Beh13, SB-Beh18, SB-Beh21, SB-Beh25).
     */
    public boolean levelLimitRisk() {
        return levelLimitRisk;
    }

    /** True while the level measuring device is out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceOutOfOrder() {
        return levelDeviceOutOfOrder;
    }

    /**
     * True while the steam measuring device or a pump controller is out of order: the units
     * the rescue-mode level estimate relies on (SB-V9, SB-FR2, SB-FR4, SB-Beh19, SB-Beh21).
     */
    public boolean steamOrPumpControllerOutOfOrder() {
        return steamOrPumpControllerOutOfOrder;
    }

    /** True while any unit other than the level measuring device is out of order (SB-V9, SB-Beh12). */
    public boolean nonLevelUnitOutOfOrder() {
        return nonLevelUnitOutOfOrder;
    }

    /** Turns this cycle's measures and unit status into the judgements the controller reads. */
    private void judge() {
        stopDemanded = successiveStopCount >= SteamBoilerConstants.STOP_REPEAT_LIMIT;
        steamRateNonZero = steamRate != 0.0;
        levelBelowNormal = levelEstimate < SteamBoilerConstants.MIN_NORMAL;
        levelAboveNormal = levelEstimate > SteamBoilerConstants.MAX_NORMAL;
        levelLimitRisk = minimumLevelForecast <= SteamBoilerConstants.MIN_LIMIT
                || maximumLevelForecast >= SteamBoilerConstants.MAX_LIMIT;
        levelDeviceOutOfOrder = units.outOfOrder(UnitStatusTable.LEVEL_UNIT);
        boolean steamOut = units.outOfOrder(UnitStatusTable.STEAM_UNIT);
        steamOrPumpControllerOutOfOrder = steamOut || units.anyPumpControllerOutOfOrder();
        nonLevelUnitOutOfOrder = steamOrPumpControllerOutOfOrder || units.anyPumpOutOfOrder();
    }

    /**
     * STEAM_BOILER_WAITING and PHYSICAL_UNITS_READY belong to initialization; outside it, or
     * PHYSICAL_UNITS_READY before any PROGRAM_READY, their presence is aberrant (SB-FR5).
     */
    private boolean startupMessageAberrant() {
        boolean startupMessage = boilerWaitingInTransmission || unitsReadyInTransmission;
        boolean initializing = lastReportedMode == OperatingMode.INITIALIZATION_MODE;
        boolean unansweredReady = unitsReadyInTransmission && !programReadySent;
        return (startupMessage && !initializing) || unansweredReady;
    }

    // =====================================================================================
    // Sorting the messages of a transmission
    // =====================================================================================

    private void startTransmission() {
        stopInTransmission = false;
        boilerWaitingInTransmission = false;
        unitsReadyInTransmission = false;
        aberrantMessage = false;
        levelMessages = 0;
        steamMessages = 0;
        for (int index = 0; index < PUMPS; index++) {
            pumpStateMessages[index] = 0;
            pumpControlMessages[index] = 0;
        }
        pendingAcknowledgements.clear();
        pendingRepairs.clear();
        repairAcknowledgements.clear();
    }

    private void sort(InputEvent message) {
        if (message instanceof InputEvent.Stop) {
            stopInTransmission = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            boilerWaitingInTransmission = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyInTransmission = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState pumpState = (InputEvent.PumpState) message;
            sortPumpState(pumpState.reading());
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState controlState = (InputEvent.PumpControlState) message;
            sortPumpControlState(controlState.reading());
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            waterLevel = level.quantity();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamRate = steam.rate();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            sortPumpIndexed(repaired.pump(), false, false);
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            sortPumpIndexed(repaired.pump(), true, false);
        } else if (message instanceof InputEvent.LevelRepaired) {
            pendingRepairs.add(UnitStatusTable.LEVEL_UNIT);
        } else if (message instanceof InputEvent.SteamRepaired) {
            pendingRepairs.add(UnitStatusTable.STEAM_UNIT);
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            sortPumpIndexed(ack.pump(), false, true);
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            sortPumpIndexed(ack.pump(), true, true);
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            sortAcknowledgement(UnitStatusTable.LEVEL_UNIT);
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            sortAcknowledgement(UnitStatusTable.STEAM_UNIT);
        }
    }

    private void sortPumpState(PumpStateReading reading) {
        if (reading == null || !validPump(reading.pumpNumber()) || reading.position() == null) {
            aberrantMessage = true;
            return;
        }
        int index = reading.pumpNumber() - 1;
        pumpStateMessages[index] = pumpStateMessages[index] + 1;
        pumps[index].readPosition(reading.position());
    }

    private void sortPumpControlState(PumpControlReading reading) {
        if (reading == null || !validPump(reading.controlledPump()) || reading.flow() == null) {
            aberrantMessage = true;
            return;
        }
        int index = reading.controlledPump() - 1;
        pumpControlMessages[index] = pumpControlMessages[index] + 1;
        pumps[index].readFlow(reading.flow());
    }

    private void sortPumpIndexed(int pump, boolean controller, boolean acknowledgement) {
        if (!validPump(pump)) {
            aberrantMessage = true;
            return;
        }
        int unit = UnitStatusTable.pumpUnit(pump);
        if (controller) {
            unit = UnitStatusTable.pumpControllerUnit(pump);
        }
        if (acknowledgement) {
            sortAcknowledgement(unit);
        } else {
            pendingRepairs.add(unit);
        }
    }

    /** An acknowledgement of a failure that was never announced, or announced and already acknowledged, is aberrant. */
    private void sortAcknowledgement(int unit) {
        if (units.awaitingAcknowledgement(unit) && !pendingAcknowledgements.contains(unit)) {
            pendingAcknowledgements.add(unit);
        } else {
            aberrantMessage = true;
        }
    }

    private static boolean validPump(int pump) {
        return pump >= 1 && pump <= PUMPS;
    }

    /** The four data messages are indispensable: exactly one of each per unit (SB-FR5). */
    private boolean dataComplete() {
        boolean complete = levelMessages == 1 && steamMessages == 1;
        for (int index = 0; index < PUMPS; index++) {
            if (pumpStateMessages[index] != 1 || pumpControlMessages[index] != 1) {
                complete = false;
            }
        }
        return complete;
    }

    // =====================================================================================
    // Analysis
    // =====================================================================================

    private void analyse() {
        for (PumpTracker tracker : pumps) {
            tracker.advanceCycle();
        }
        for (Integer unit : pendingAcknowledgements) {
            units.acknowledge(unit);
        }
        for (Integer unit : pendingRepairs) {
            applyRepair(unit);
        }
        detectSteamFailure();
        detectPumpFailures();
        countPouringPumps();
        detectLevelFailure();
        updateLevelEstimate();
        updateForecasts();
        rememberCycle();
    }

    /** SB-FR7: the unit is sound again and its repair is acknowledged in this cycle. */
    private void applyRepair(int unit) {
        units.repair(unit);
        if (UnitStatusTable.isPumpUnit(unit)) {
            int pump = UnitStatusTable.pumpOf(unit);
            pumps[pump - 1].forget();
            repairAcknowledgements.add(new OutputEvent.PumpRepairedAcknowledgement(pump));
        } else if (UnitStatusTable.isPumpControllerUnit(unit)) {
            int pump = UnitStatusTable.pumpOf(unit);
            pumps[pump - 1].forget();
            repairAcknowledgements.add(new OutputEvent.PumpControlRepairedAcknowledgement(pump));
        } else if (unit == UnitStatusTable.LEVEL_UNIT) {
            repairAcknowledgements.add(new OutputEvent.LevelRepairedAcknowledgement());
        } else {
            repairAcknowledgements.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
    }

    /** SB-FR4: outside [0, W], or changing faster than the gradients U1 and U2 allow. */
    private void detectSteamFailure() {
        if (units.outOfOrder(UnitStatusTable.STEAM_UNIT)) {
            return;
        }
        boolean outOfRange = steamRate < 0.0 || steamRate > SteamBoilerConstants.MAX_STEAM_RATE;
        boolean tooFast = false;
        if (previousSteamValid) {
            boolean riseTooFast =
                    steamRate - previousSteamRate > CYCLE * SteamBoilerConstants.MAX_STEAM_INCREASE;
            boolean fallTooFast =
                    previousSteamRate - steamRate > CYCLE * SteamBoilerConstants.MAX_STEAM_DECREASE;
            tooFast = riseTooFast || fallTooFast;
        }
        if (outOfRange || tooFast) {
            units.detect(UnitStatusTable.STEAM_UNIT);
        }
    }

    /** SB-FR1 and SB-FR2, pump by pump. */
    private void detectPumpFailures() {
        for (int pump = 1; pump <= PUMPS; pump++) {
            PumpTracker tracker = pumps[pump - 1];
            int pumpUnit = UnitStatusTable.pumpUnit(pump);
            int controllerUnit = UnitStatusTable.pumpControllerUnit(pump);
            if (!units.outOfOrder(pumpUnit) && tracker.pumpFault()) {
                units.detect(pumpUnit);
            }
            boolean pumpSound = !units.outOfOrder(pumpUnit);
            if (!units.outOfOrder(controllerUnit) && tracker.controllerFault(pumpSound)) {
                units.detect(controllerUnit);
            }
        }
    }

    /**
     * SB-V7: p from the reported pump states and pump-controller states. A pump surely pours
     * when its sound devices say so; it possibly pours unless a sound device says it does not.
     */
    private void countPouringPumps() {
        surePouring = 0;
        possiblePouring = 0;
        stuckPouring = 0;
        for (int pump = 1; pump <= PUMPS; pump++) {
            PumpTracker tracker = pumps[pump - 1];
            boolean pumpSound = !units.outOfOrder(UnitStatusTable.pumpUnit(pump));
            boolean controllerSound = !units.outOfOrder(UnitStatusTable.pumpControllerUnit(pump));
            boolean sure = false;
            boolean possible = true;
            if (pumpSound && controllerSound) {
                sure = tracker.indicatesOn() && tracker.indicatesFlow();
                possible = sure;
            } else if (controllerSound) {
                sure = tracker.indicatesFlow();
                possible = sure;
            } else if (pumpSound) {
                sure = tracker.indicatesOn() && tracker.settled();
                possible = tracker.indicatesOn();
            }
            if (sure) {
                surePouring = surePouring + 1;
            }
            if (possible) {
                possiblePouring = possiblePouring + 1;
            }
            if (possible && !pumpSound) {
                stuckPouring = stuckPouring + 1;
            }
        }
        pumpThroughput = SteamBoilerConstants.PUMP_CAPACITY * surePouring;
    }

    /**
     * SB-FR3: outside [0, C], or a value the level could not have reached from the previous
     * cycle even under extreme behaviour of steam and pumps.
     */
    private void detectLevelFailure() {
        if (units.outOfOrder(UnitStatusTable.LEVEL_UNIT)) {
            return;
        }
        boolean outOfRange = waterLevel < 0.0 || waterLevel > SteamBoilerConstants.CAPACITY;
        boolean implausible = false;
        if (previousLevelValid) {
            double lowest = previousLowerBound + pastInflowMin() - pastSteamOutflowMax();
            double highest = previousUpperBound + pastInflowMax() - pastSteamOutflowMin();
            implausible = waterLevel < lowest || waterLevel > highest;
        }
        if (outOfRange || implausible) {
            units.detect(UnitStatusTable.LEVEL_UNIT);
        }
    }

    /**
     * SB-V8, SB-Beh19: while the level device is sound the estimate is q itself; while it is
     * out of order the bounds are advanced from the previous ones with the extreme steam
     * outflow and pump inflow of the elapsed cycle, each pumped litre counting as one litre of
     * boiler content.
     */
    private void updateLevelEstimate() {
        if (!units.outOfOrder(UnitStatusTable.LEVEL_UNIT)) {
            levelLowerBound = waterLevel;
            levelUpperBound = waterLevel;
        } else if (estimateValid) {
            levelLowerBound =
                    Math.max(0.0, previousLowerBound + pastInflowMin() - pastSteamOutflowMax());
            levelUpperBound = Math.min(SteamBoilerConstants.CAPACITY,
                    previousUpperBound + pastInflowMax() - pastSteamOutflowMin());
        } else {
            levelLowerBound = 0.0;
            levelUpperBound = SteamBoilerConstants.CAPACITY;
        }
        levelEstimate = (levelLowerBound + levelUpperBound) / 2.0;
    }

    /** SB-V4, SB-Beh13, SB-Beh25: worst-case level one cycle ahead. */
    private void updateForecasts() {
        double nextOutflowMax = CYCLE * SteamBoilerConstants.MAX_STEAM_RATE;
        double nextOutflowMin = 0.0;
        if (!units.outOfOrder(UnitStatusTable.STEAM_UNIT)) {
            nextOutflowMax = Math.min(CYCLE * SteamBoilerConstants.MAX_STEAM_RATE,
                    CYCLE * steamRate + CYCLE * CYCLE * SteamBoilerConstants.MAX_STEAM_INCREASE / 2.0);
            nextOutflowMin = Math.max(0.0,
                    CYCLE * steamRate - CYCLE * CYCLE * SteamBoilerConstants.MAX_STEAM_DECREASE / 2.0);
        }
        double pumpVolume = CYCLE * SteamBoilerConstants.PUMP_CAPACITY;
        minimumLevelForecast =
                Math.max(0.0, levelLowerBound + pumpVolume * surePouring - nextOutflowMax);
        maximumLevelForecast = levelUpperBound + pumpVolume * stuckPouring - nextOutflowMin;
    }

    private void rememberCycle() {
        previousSteamRate = steamRate;
        previousSteamValid = !units.outOfOrder(UnitStatusTable.STEAM_UNIT);
        previousLevelValid = !units.outOfOrder(UnitStatusTable.LEVEL_UNIT);
        previousLowerBound = levelLowerBound;
        previousUpperBound = levelUpperBound;
        estimateValid = true;
        previousSurePouring = surePouring;
        previousPossiblePouring = possiblePouring;
        for (PumpTracker tracker : pumps) {
            tracker.endCycle();
        }
    }

    private void orderPump(int pump, PumpPosition target) {
        if (validPump(pump)) {
            pumps[pump - 1].order(target);
        }
    }

    // ---- extreme water movements over the elapsed cycle ----

    private boolean steamKnownThroughout() {
        return previousSteamValid && !units.outOfOrder(UnitStatusTable.STEAM_UNIT);
    }

    /** Steam volume if the rate moved linearly between the two readings. */
    @RoboChartType("real")
    private double steamChord() {
        return CYCLE * (previousSteamRate + steamRate) / 2.0;
    }

    /** Largest deviation from the chord that the gradients U1 and U2 permit within one cycle. */
    @RoboChartType("real")
    private double steamSlack() {
        double up = SteamBoilerConstants.MAX_STEAM_INCREASE;
        double down = SteamBoilerConstants.MAX_STEAM_DECREASE;
        return CYCLE * CYCLE * up * down / (2.0 * (up + down));
    }

    @RoboChartType("real")
    private double pastSteamOutflowMax() {
        if (steamKnownThroughout()) {
            return Math.min(CYCLE * SteamBoilerConstants.MAX_STEAM_RATE, steamChord() + steamSlack());
        }
        return CYCLE * SteamBoilerConstants.MAX_STEAM_RATE;
    }

    @RoboChartType("real")
    private double pastSteamOutflowMin() {
        if (steamKnownThroughout()) {
            return Math.max(0.0, steamChord() - steamSlack());
        }
        return 0.0;
    }

    @RoboChartType("real")
    private double pastInflowMin() {
        return CYCLE * SteamBoilerConstants.PUMP_CAPACITY * Math.min(previousSurePouring, surePouring);
    }

    @RoboChartType("real")
    private double pastInflowMax() {
        return CYCLE * SteamBoilerConstants.PUMP_CAPACITY
                * Math.max(previousPossiblePouring, possiblePouring);
    }
}
