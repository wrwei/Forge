package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.FlowReport;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.PumpReport;
import steam_boiler.domain.WaterFlow;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the physical units, rebuilt at the start of every cycle.
 *
 * <p>{@link #receive} performs the first stage of a cycle (SB-V3): it decodes the cycle's
 * transmission, judges the transmission system (SB-FR5) and each unit (SB-FR1 to SB-FR4), keeps the
 * per-unit failure, acknowledgement and repair bookkeeping (SB-V9, SB-FR6, SB-FR7), counts
 * successive STOP messages (SB-V10), and computes the believed water level and its worst-case
 * evolution over the next cycle (SB-V4 to SB-V8, SB-Beh25). The getters then publish the results to
 * the controller for the rest of the cycle.
 */
public final class BoilerSensor {

    private final UnitStatus status = new UnitStatus();
    private final List<OutputEvent> unitMessages = new ArrayList<>();

    // Content of the current transmission.
    private boolean stopPresent = false;
    private boolean boilerWaitingPresent = false;
    private boolean unitsReadyPresent = false;
    private boolean structuralFault = false;
    private boolean aberrantMessage = false;
    @RoboChartType("nat") private int levelMessages = 0;
    @RoboChartType("nat") private int steamMessages = 0;
    @RoboChartType("real") private double levelValue = 0.0;
    @RoboChartType("real") private double steamValue = 0.0;
    private final int[] pumpStateMessages = new int[BoilerConstants.PUMP_COUNT];
    private final int[] flowStateMessages = new int[BoilerConstants.PUMP_COUNT];
    private final boolean[] reportedOn = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] reportedFlow = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] repairReceived = new boolean[UnitStatus.UNIT_COUNT];
    private final boolean[] acknowledgementReceived = new boolean[UnitStatus.UNIT_COUNT];

    // Orders the program issued at the end of the previous cycle.
    private final boolean[] orderedOpen = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] orderedClose = new boolean[BoilerConstants.PUMP_COUNT];
    private boolean valveOpen = false;

    // What the program remembers of the previous cycle.
    @RoboChartType("nat") private int stopRun = 0;
    private boolean plantWaitingSeen = false;
    private boolean levelKnown = false;
    private boolean steamKnown = false;
    @RoboChartType("real") private double previousLow = 0.0;
    @RoboChartType("real") private double previousHigh = 0.0;
    @RoboChartType("real") private double previousSteam = 0.0;
    @RoboChartType("real") private double previousMaxSteamOut = 0.0;
    @RoboChartType("real") private double previousMinSteamOut = 0.0;
    private final boolean[] pumpKnown = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] previousOn = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] previousPossible = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] previousFlowing = new boolean[BoilerConstants.PUMP_COUNT];

    // Quantities published for the current cycle.
    private boolean transmissionFault = false;
    private boolean levelDefect = false;
    private boolean steamDefect = false;
    private boolean pumpDefect = false;
    private boolean controlDefect = false;
    @RoboChartType("nat") private int fillPumpNumber = 1;
    @RoboChartType("real") private double throughput = 0.0;
    @RoboChartType("real") private double lowerBound = 0.0;
    @RoboChartType("real") private double upperBound = 0.0;
    @RoboChartType("real") private double estimate = 0.0;
    @RoboChartType("real") private double nextMaxSteamOut = 0.0;
    @RoboChartType("real") private double nextMinSteamOut = 0.0;
    @RoboChartType("real") private double predictedMin = 0.0;
    @RoboChartType("real") private double predictedMax = 0.0;

    /**
     * Receives one cycle's transmission, whose messages are all considered simultaneous, together
     * with the messages the program emitted at the end of the previous cycle, and recomputes every
     * published quantity. Failure judgements start once STEAM_BOILER_WAITING has arrived (SB-Beh2).
     */
    public void receive(List<InputEvent> transmission, List<OutputEvent> previousOrders) {
        unitMessages.clear();
        decode(transmission);
        if (stopPresent) {
            stopRun = stopRun + 1;
        } else {
            stopRun = 0;
        }
        if (boilerWaitingPresent) {
            plantWaitingSeen = true;
        }
        recordOrders(previousOrders);
        processAcknowledgements();
        processRepairs();
        if (!structuralFault) {
            if (plantWaitingSeen) {
                detectFailures();
            }
            computeLevelBounds();
            if (plantWaitingSeen) {
                rememberCycle();
            }
        }
        status.addAnnouncements(unitMessages);
        publish();
    }

    /**
     * The per-unit messages of this cycle: failure detections not yet acknowledged (SB-FR6) and
     * acknowledgements of the repairs received (SB-FR7).
     */
    public List<OutputEvent> unitMessages() {
        return List.copyOf(unitMessages);
    }

    /** Number of successive transmissions, up to this one, that carried STOP (SB-V10). */
    @RoboChartType("nat")
    public int consecutiveStops() {
        return stopRun;
    }

    /** Whether the transmission system has failed in this cycle (SB-FR5). */
    public boolean transmissionFailure() {
        return transmissionFault;
    }

    /** Whether this transmission carried STEAM_BOILER_WAITING. */
    public boolean steamBoilerWaitingReceived() {
        return boilerWaitingPresent;
    }

    /** Whether this transmission carried PHYSICAL_UNITS_READY. */
    public boolean physicalUnitsReadyReceived() {
        return unitsReadyPresent;
    }

    /** Whether STEAM_BOILER_WAITING has arrived, in this transmission or an earlier one. */
    public boolean plantWaiting() {
        return plantWaitingSeen;
    }

    /** v: the steam rate reported in this transmission (litre/sec) (SB-V6). */
    @RoboChartType("real")
    public double steamReading() {
        return steamValue;
    }

    /** q: the water level reported in this transmission (litre) (SB-V5). */
    @RoboChartType("real")
    public double levelReading() {
        return levelValue;
    }

    /** Whether the level measuring device is out of order (SB-V9). */
    public boolean levelDeviceOutOfOrder() {
        return levelDefect;
    }

    /** Whether the steam measuring device is out of order (SB-V9). */
    public boolean steamDeviceOutOfOrder() {
        return steamDefect;
    }

    /** Whether at least one pump is out of order (SB-V9). */
    public boolean pumpUnitOutOfOrder() {
        return pumpDefect;
    }

    /** Whether at least one pump controller is out of order (SB-V9). */
    public boolean pumpControlUnitOutOfOrder() {
        return controlDefect;
    }

    /** Lower bound on the true water level now: q, or the estimate in its absence (SB-V8). */
    @RoboChartType("real")
    public double levelLowerBound() {
        return lowerBound;
    }

    /** Upper bound on the true water level now: q, or the estimate in its absence (SB-V8). */
    @RoboChartType("real")
    public double levelUpperBound() {
        return upperBound;
    }

    /** The level the program regulates on: q, or the middle of the estimate (SB-V5, SB-V8). */
    @RoboChartType("real")
    public double levelEstimate() {
        return estimate;
    }

    /**
     * Worst-case lowest level at the next cycle: steam rising at its maximal gradient and no water
     * delivered by the pumps (SB-V4, SB-DM1).
     */
    @RoboChartType("real")
    public double predictedLevelMin() {
        return predictedMin;
    }

    /**
     * Worst-case highest level at the next cycle: steam falling at its maximal gradient and every
     * pump that is on, or whose state is unknown, delivering its capacity (SB-V4, SB-DM1).
     */
    @RoboChartType("real")
    public double predictedLevelMax() {
        return predictedMax;
    }

    /** p: current throughput of the pumps, from pump and pump-controller states (SB-V7). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return throughput;
    }

    /** The lowest-numbered pump whose pump and controller are both sound; pump 1 if none is. */
    @RoboChartType("nat")
    public int fillPump() {
        return fillPumpNumber;
    }

    private void decode(List<InputEvent> transmission) {
        stopPresent = false;
        boilerWaitingPresent = false;
        unitsReadyPresent = false;
        structuralFault = false;
        aberrantMessage = false;
        levelMessages = 0;
        steamMessages = 0;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumpStateMessages[i] = 0;
            flowStateMessages[i] = 0;
        }
        for (int unit = 0; unit < UnitStatus.UNIT_COUNT; unit++) {
            repairReceived[unit] = false;
            acknowledgementReceived[unit] = false;
        }
        for (InputEvent message : transmission) {
            decodeMessage(message);
        }
        // The four data messages are indispensable, exactly once each (SB-FR5).
        if (levelMessages != 1 || steamMessages != 1) {
            structuralFault = true;
        }
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (pumpStateMessages[i] != 1 || flowStateMessages[i] != 1) {
                structuralFault = true;
            }
        }
    }

    private void decodeMessage(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopPresent = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            boilerWaitingPresent = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyPresent = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState pumpState = (InputEvent.PumpState) message;
            decodePumpState(pumpState.report());
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState controlState = (InputEvent.PumpControlState) message;
            decodeFlowState(controlState.report());
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
            markPumpUnit(repairReceived, UnitStatus.PUMP_BASE, repaired.pump());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            markPumpUnit(repairReceived, UnitStatus.CONTROL_BASE, repaired.pump());
        } else if (message instanceof InputEvent.LevelRepaired) {
            repairReceived[UnitStatus.LEVEL_UNIT] = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            repairReceived[UnitStatus.STEAM_UNIT] = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            markPumpUnit(acknowledgementReceived, UnitStatus.PUMP_BASE, ack.pump());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            markPumpUnit(acknowledgementReceived, UnitStatus.CONTROL_BASE, ack.pump());
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            acknowledgementReceived[UnitStatus.LEVEL_UNIT] = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            acknowledgementReceived[UnitStatus.STEAM_UNIT] = true;
        }
        // TransmissionReceived marks the cycle for the controller; it is not a unit's message.
    }

    private void decodePumpState(PumpReport report) {
        if (report == null || report.position() == null || !isPumpNumber(report.pumpNumber())) {
            structuralFault = true;
            return;
        }
        int index = report.pumpNumber() - 1;
        pumpStateMessages[index] = pumpStateMessages[index] + 1;
        reportedOn[index] = report.position() == PumpPosition.PUMP_ON;
    }

    private void decodeFlowState(FlowReport report) {
        if (report == null || report.flow() == null || !isPumpNumber(report.controllerNumber())) {
            structuralFault = true;
            return;
        }
        int index = report.controllerNumber() - 1;
        flowStateMessages[index] = flowStateMessages[index] + 1;
        reportedFlow[index] = report.flow() == WaterFlow.FLOW_PRESENT;
    }

    private void markPumpUnit(boolean[] received, int base, int pumpNumber) {
        if (isPumpNumber(pumpNumber)) {
            received[base + pumpNumber - 1] = true;
        } else {
            structuralFault = true;
        }
    }

    private static boolean isPumpNumber(int pumpNumber) {
        return pumpNumber >= 1 && pumpNumber <= BoilerConstants.PUMP_COUNT;
    }

    private void recordOrders(List<OutputEvent> previousOrders) {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            orderedOpen[i] = false;
            orderedClose[i] = false;
        }
        for (OutputEvent order : previousOrders) {
            if (order instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump open = (OutputEvent.OpenPump) order;
                if (isPumpNumber(open.pump())) {
                    orderedOpen[open.pump() - 1] = true;
                }
            } else if (order instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump close = (OutputEvent.ClosePump) order;
                if (isPumpNumber(close.pump())) {
                    orderedClose[close.pump() - 1] = true;
                }
            } else if (order instanceof OutputEvent.Valve) {
                valveOpen = !valveOpen;
            }
        }
    }

    // An acknowledgement for a failure that was never announced is aberrant (SB-FR5, SB-FR6).
    private void processAcknowledgements() {
        for (int unit = 0; unit < UnitStatus.UNIT_COUNT; unit++) {
            if (acknowledgementReceived[unit]) {
                if (status.isAwaitingAcknowledgement(unit)) {
                    status.recordAcknowledgement(unit);
                } else {
                    aberrantMessage = true;
                }
            }
        }
    }

    // A repair of a unit that is not out of order is aberrant (SB-FR5, SB-FR7).
    private void processRepairs() {
        for (int unit = 0; unit < UnitStatus.UNIT_COUNT; unit++) {
            if (repairReceived[unit]) {
                if (status.isOutOfOrder(unit)) {
                    status.recordRepair(unit);
                    forgetHistory(unit);
                    unitMessages.add(UnitStatus.repairAcknowledgement(unit));
                } else {
                    aberrantMessage = true;
                }
            }
        }
    }

    private void forgetHistory(int unit) {
        if (unit < UnitStatus.CONTROL_BASE) {
            pumpKnown[unit - UnitStatus.PUMP_BASE] = false;
        } else if (unit == UnitStatus.STEAM_UNIT) {
            steamKnown = false;
        }
    }

    private void detectFailures() {
        detectSteamFailure();
        detectLevelFailure();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            detectPumpFailure(i);
        }
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            detectPumpControlFailure(i);
        }
    }

    // SB-FR4: outside [0, W], or changing faster than the gradients U1 and U2 allow.
    private void detectSteamFailure() {
        if (status.isOutOfOrder(UnitStatus.STEAM_UNIT)) {
            return;
        }
        boolean outsideRange = steamValue < 0.0 || steamValue > BoilerConstants.STEAM_MAX_W;
        boolean tooFast = false;
        if (steamKnown) {
            double highest = previousSteam + BoilerConstants.STEAM_RISE_U1 * BoilerConstants.CYCLE_SECONDS;
            double lowest = previousSteam - BoilerConstants.STEAM_FALL_U2 * BoilerConstants.CYCLE_SECONDS;
            tooFast = steamValue > highest || steamValue < lowest;
        }
        if (outsideRange || tooFast) {
            status.recordFailure(UnitStatus.STEAM_UNIT);
        }
    }

    // SB-FR3: outside [0, C], or a level the boiler could not have reached since the previous cycle
    // even with extreme steam and pump behaviour. While the evacuation valve was open no lower
    // bound is known, so only the upper bound is checked.
    private void detectLevelFailure() {
        if (status.isOutOfOrder(UnitStatus.LEVEL_UNIT)) {
            return;
        }
        boolean outsideRange = levelValue < 0.0 || levelValue > BoilerConstants.CAPACITY_C;
        boolean unreachable = false;
        if (levelKnown) {
            double reachableMin = previousLow - previousMaxSteamOut;
            double reachableMax = previousHigh + possibleInflow() - previousMinSteamOut;
            boolean belowReach = !valveOpen && levelValue < reachableMin;
            unreachable = belowReach || levelValue > reachableMax;
        }
        if (outsideRange || unreachable) {
            status.recordFailure(UnitStatus.LEVEL_UNIT);
        }
    }

    // SB-FR1: in the transmission after an order the pump must show the ordered state; without an
    // order it must keep its previous state.
    private void detectPumpFailure(int index) {
        int unit = UnitStatus.PUMP_BASE + index;
        if (status.isOutOfOrder(unit) || !pumpKnown[index]) {
            return;
        }
        boolean expectedOn = previousOn[index];
        if (orderedOpen[index]) {
            expectedOn = true;
        }
        if (orderedClose[index]) {
            expectedOn = false;
        }
        if (reportedOn[index] != expectedOn) {
            status.recordFailure(unit);
        }
    }

    // SB-FR2: with the pump known to work, flow must match the pump state, except in the first
    // transmission after an order that changed the pump's state; by the second transmission the
    // flow must have come or ceased (start-up latency, SB-Beh25).
    private void detectPumpControlFailure(int index) {
        int unit = UnitStatus.CONTROL_BASE + index;
        int pumpUnit = UnitStatus.PUMP_BASE + index;
        if (status.isOutOfOrder(unit) || status.isOutOfOrder(pumpUnit) || !pumpKnown[index]) {
            return;
        }
        boolean starting = orderedOpen[index] && !previousOn[index];
        boolean stopping = orderedClose[index] && previousOn[index];
        boolean settling = starting || stopping;
        if (!settling && reportedFlow[index] != reportedOn[index]) {
            status.recordFailure(unit);
        }
    }

    private boolean flowingNow(int index) {
        return !status.isOutOfOrder(UnitStatus.PUMP_BASE + index)
                && !status.isOutOfOrder(UnitStatus.CONTROL_BASE + index)
                && reportedOn[index]
                && reportedFlow[index];
    }

    // Water known to have been supplied since the previous cycle: pumps seen flowing at both ends
    // of the cycle and not closed in between (SB-Beh19).
    private double guaranteedInflow() {
        int pumps = 0;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (previousFlowing[i] && !orderedClose[i] && flowingNow(i)) {
                pumps = pumps + 1;
            }
        }
        return BoilerConstants.PUMP_CAPACITY_P * BoilerConstants.CYCLE_SECONDS * pumps;
    }

    // Most water that can have been supplied since the previous cycle: pumps that were on (or of
    // unknown state) and not closed. A pump ordered on at the previous cycle pours nothing during
    // its first five seconds (SB-DM3, SB-Beh25).
    private double possibleInflow() {
        int pumps = 0;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (previousPossible[i] && !orderedClose[i]) {
                pumps = pumps + 1;
            }
        }
        return BoilerConstants.PUMP_CAPACITY_P * BoilerConstants.CYCLE_SECONDS * pumps;
    }

    private void computeLevelBounds() {
        int flowing = 0;
        int possible = 0;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (flowingNow(i)) {
                flowing = flowing + 1;
            }
            if (reportedOn[i] || status.isOutOfOrder(UnitStatus.PUMP_BASE + i)) {
                possible = possible + 1;
            }
        }
        throughput = BoilerConstants.PUMP_CAPACITY_P * flowing;

        if (!status.isOutOfOrder(UnitStatus.LEVEL_UNIT)) {
            lowerBound = levelValue;
            upperBound = levelValue;
        } else if (levelKnown) {
            // SB-Beh19: advance the previous bounds by the worst-case steam outcome and the water
            // the pumps are known (lower) or able (upper) to have supplied, one litre for one litre.
            double low = previousLow + guaranteedInflow() - previousMaxSteamOut;
            if (valveOpen) {
                low = 0.0;
            }
            double high = previousHigh + possibleInflow() - previousMinSteamOut;
            lowerBound = Math.max(0.0, low);
            upperBound = Math.max(lowerBound, Math.min(BoilerConstants.CAPACITY_C, high));
        } else {
            lowerBound = 0.0;
            upperBound = BoilerConstants.CAPACITY_C;
        }
        estimate = (lowerBound + upperBound) / 2.0;

        double halfSquare = 0.5 * BoilerConstants.CYCLE_SECONDS * BoilerConstants.CYCLE_SECONDS;
        if (!status.isOutOfOrder(UnitStatus.STEAM_UNIT)) {
            nextMaxSteamOut = Math.min(
                    BoilerConstants.STEAM_MAX_W * BoilerConstants.CYCLE_SECONDS,
                    steamValue * BoilerConstants.CYCLE_SECONDS + BoilerConstants.STEAM_RISE_U1 * halfSquare);
            nextMinSteamOut = Math.max(
                    0.0,
                    steamValue * BoilerConstants.CYCLE_SECONDS - BoilerConstants.STEAM_FALL_U2 * halfSquare);
        } else {
            nextMaxSteamOut = BoilerConstants.STEAM_MAX_W * BoilerConstants.CYCLE_SECONDS;
            nextMinSteamOut = 0.0;
        }
        predictedMin = lowerBound - nextMaxSteamOut;
        predictedMax = upperBound
                + BoilerConstants.PUMP_CAPACITY_P * BoilerConstants.CYCLE_SECONDS * possible
                - nextMinSteamOut;
    }

    private void rememberCycle() {
        previousLow = lowerBound;
        previousHigh = upperBound;
        levelKnown = true;
        previousSteam = steamValue;
        steamKnown = !status.isOutOfOrder(UnitStatus.STEAM_UNIT);
        previousMaxSteamOut = nextMaxSteamOut;
        previousMinSteamOut = nextMinSteamOut;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            boolean pumpOutOfOrder = status.isOutOfOrder(UnitStatus.PUMP_BASE + i);
            previousOn[i] = reportedOn[i];
            pumpKnown[i] = !pumpOutOfOrder;
            previousPossible[i] = reportedOn[i] || pumpOutOfOrder;
            previousFlowing[i] = flowingNow(i);
        }
    }

    private void publish() {
        transmissionFault = structuralFault || aberrantMessage;
        levelDefect = status.isOutOfOrder(UnitStatus.LEVEL_UNIT);
        steamDefect = status.isOutOfOrder(UnitStatus.STEAM_UNIT);
        pumpDefect = status.anyOutOfOrder(UnitStatus.PUMP_BASE, BoilerConstants.PUMP_COUNT);
        controlDefect = status.anyOutOfOrder(UnitStatus.CONTROL_BASE, BoilerConstants.PUMP_COUNT);
        fillPumpNumber = 1;
        boolean chosen = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            boolean usable = !status.isOutOfOrder(UnitStatus.PUMP_BASE + i)
                    && !status.isOutOfOrder(UnitStatus.CONTROL_BASE + i);
            if (usable && !chosen) {
                fillPumpNumber = i + 1;
                chosen = true;
            }
        }
    }
}
