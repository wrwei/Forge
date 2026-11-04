package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpId;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the plant. Each cycle it receives the incoming
 * messages, then analyses them: it checks the transmission, applies
 * acknowledgements and repairs, detects failed units, derives the pump
 * throughput, keeps the water level (or, while the level device is out of
 * order, an estimate bounded below and above), and predicts the extreme
 * levels the next cycle could reach. The controller reads the results through
 * the query methods.
 */
public final class BoilerSensor {

    private final BoilerActuator actuator;
    private final PumpUnit[] pumps = new PumpUnit[BoilerConstants.PUMP_COUNT];
    private final UnitStatus levelDevice = new UnitStatus();
    private final UnitStatus steamDevice = new UnitStatus();

    // Messages of the current cycle.
    @RoboChartType("nat") private int levelMessages = 0;
    @RoboChartType("nat") private int steamMessages = 0;
    @RoboChartType("real") private double levelReading = 0.0;
    @RoboChartType("real") private double steamReading = 0.0;
    private boolean stopReceived = false;
    private boolean steamBoilerWaitingReceived = false;
    private boolean physicalUnitsReadyReceived = false;
    private boolean levelRepairedReceived = false;
    private boolean steamRepairedReceived = false;
    private boolean levelAcknowledgementReceived = false;
    private boolean steamAcknowledgementReceived = false;

    // Results of the analysis.
    @RoboChartType("nat") private int successiveStops = 0;
    private boolean steamBoilerWaitingSeen = false;
    private boolean programReadyAnnounced = false;
    private boolean valveOpen = false;
    private boolean transmissionFailure = false;
    private boolean levelDeviceFailed = false;
    private boolean steamDeviceFailed = false;
    private boolean pumpFailed = false;
    private boolean pumpControlFailed = false;
    @RoboChartType("real") private double pumpThroughput = 0.0;
    @RoboChartType("real") private double levelLowerBound = 0.0;
    @RoboChartType("real") private double levelUpperBound = 0.0;
    @RoboChartType("real") private double levelEstimate = 0.0;
    @RoboChartType("real") private double steamLowerBound = 0.0;
    @RoboChartType("real") private double steamUpperBound = 0.0;
    @RoboChartType("real") private double worstCaseMinimumLevel = 0.0;
    @RoboChartType("real") private double worstCaseMaximumLevel = 0.0;
    private boolean historyKnown = false;

    /**
     * Creates the sensor.
     *
     * @param actuator where the program's own previous orders are recorded
     */
    public BoilerSensor(BoilerActuator actuator) {
        this.actuator = actuator;
        PumpId[] ids = PumpId.values();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps[i] = new PumpUnit(ids[i]);
        }
    }

    /** Starts the reception stage of a new cycle. */
    public void beginCycle() {
        levelMessages = 0;
        steamMessages = 0;
        stopReceived = false;
        steamBoilerWaitingReceived = false;
        physicalUnitsReadyReceived = false;
        levelRepairedReceived = false;
        steamRepairedReceived = false;
        levelAcknowledgementReceived = false;
        steamAcknowledgementReceived = false;
        levelDevice.beginCycle();
        steamDevice.beginCycle();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps[i].beginCycle();
        }
    }

    /**
     * Receives one message of the current cycle's transmission.
     *
     * @param message the message received
     */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.OperatorStop) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            steamBoilerWaitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            physicalUnitsReadyReceived = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            pumps[state.status().pump().ordinal()].receiveState(state.status().switchedOn());
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState state = (InputEvent.PumpControlState) message;
            pumps[state.status().pump().ordinal()].receiveControlState(state.status().waterFlowing());
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level reading = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            levelReading = reading.quantity();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam reading = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamReading = reading.rate();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            pumps[repaired.pump().ordinal()].receivePumpRepaired();
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            pumps[repaired.pump().ordinal()].receiveControlRepaired();
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairedReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairedReceived = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            pumps[ack.pump().ordinal()].receivePumpAcknowledgement();
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack = (InputEvent.PumpControlFailureAcknowledgement) message;
            pumps[ack.pump().ordinal()].receiveControlAcknowledgement();
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAcknowledgementReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAcknowledgementReceived = true;
        }
    }

    /** Analyses the messages of the current cycle. */
    public void analyse() {
        if (stopReceived) {
            successiveStops = successiveStops + 1;
        } else {
            successiveStops = 0;
        }
        if (steamBoilerWaitingReceived) {
            steamBoilerWaitingSeen = true;
        }
        programReadyAnnounced = actuator.programReadySent();
        valveOpen = actuator.valveOpen();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            PumpId id = PumpId.values()[i];
            pumps[i].recordOrders(actuator.openOrderedLast(id), actuator.closeOrderedLast(id));
        }

        boolean acknowledgementsValid = processAcknowledgements();
        transmissionFailure = !dataComplete() || !acknowledgementsValid;
        if (!transmissionFailure) {
            processRepairs();
            if (steamBoilerWaitingSeen) {
                analyseUnits();
            }
        }

        levelDeviceFailed = levelDevice.failed();
        steamDeviceFailed = steamDevice.failed();
        pumpFailed = false;
        pumpControlFailed = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (pumps[i].pumpFailed()) {
                pumpFailed = true;
            }
            if (pumps[i].controlFailed()) {
                pumpControlFailed = true;
            }
        }
    }

    /** Every data message arrived exactly once. */
    private boolean dataComplete() {
        boolean complete = levelMessages == 1 && steamMessages == 1;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (!pumps[i].dataComplete()) {
                complete = false;
            }
        }
        return complete;
    }

    private boolean processAcknowledgements() {
        boolean valid = true;
        if (levelAcknowledgementReceived && !levelDevice.acknowledgeFailure()) {
            valid = false;
        }
        if (steamAcknowledgementReceived && !steamDevice.acknowledgeFailure()) {
            valid = false;
        }
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (!pumps[i].processAcknowledgements()) {
                valid = false;
            }
        }
        return valid;
    }

    private void processRepairs() {
        if (levelRepairedReceived) {
            levelDevice.repair();
        }
        if (steamRepairedReceived) {
            steamDevice.repair();
        }
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps[i].processRepairs();
        }
    }

    private void analyseUnits() {
        double previousSteamLow = steamLowerBound;
        double previousSteamHigh = steamUpperBound;

        // Steam measuring device: static range, then the gradients U1 and U2.
        boolean steamOutOfRange = steamReading < 0.0 || steamReading > BoilerConstants.MAX_STEAM;
        boolean steamTooFast = historyKnown
                && (steamReading > previousSteamHigh + BoilerConstants.STEAM_RISE_GRADIENT * BoilerConstants.CYCLE_DURATION
                    || steamReading < previousSteamLow - BoilerConstants.STEAM_FALL_GRADIENT * BoilerConstants.CYCLE_DURATION);
        if (!steamDevice.failed() && (steamOutOfRange || steamTooFast)) {
            steamDevice.detectFailure();
        }
        if (!steamDevice.failed()) {
            steamLowerBound = steamReading;
            steamUpperBound = steamReading;
        } else if (historyKnown) {
            steamLowerBound = Math.max(0.0,
                    previousSteamLow - BoilerConstants.STEAM_FALL_GRADIENT * BoilerConstants.CYCLE_DURATION);
            steamUpperBound = Math.min(BoilerConstants.MAX_STEAM,
                    previousSteamHigh + BoilerConstants.STEAM_RISE_GRADIENT * BoilerConstants.CYCLE_DURATION);
        } else {
            steamLowerBound = 0.0;
            steamUpperBound = BoilerConstants.MAX_STEAM;
        }

        // Pumps and pump controllers.
        double inflowMin = 0.0;
        double inflowMax = 0.0;
        double nextInflowMax = 0.0;
        int pouring = 0;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            PumpUnit unit = pumps[i];
            unit.detectFailures();
            inflowMin = inflowMin + unit.minimalInflow();
            inflowMax = inflowMax + unit.maximalInflow();
            if (unit.mayPourNext()) {
                nextInflowMax = nextInflowMax + BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION;
            }
            if (unit.pouringNow()) {
                pouring = pouring + 1;
            }
        }
        pumpThroughput = BoilerConstants.PUMP_CAPACITY * pouring;

        // Water level: the range reachable from the last cycle under extreme
        // steam and pump behaviour (the evacuation valve drains at no known rate).
        double feasibleLow = 0.0;
        double feasibleHigh = BoilerConstants.CAPACITY;
        if (historyKnown) {
            feasibleLow = levelLowerBound + inflowMin - maximalSteamOutflow(previousSteamHigh);
            feasibleHigh = levelUpperBound + inflowMax - minimalSteamOutflow(previousSteamLow);
            if (valveOpen) {
                feasibleLow = 0.0;
            }
        }
        feasibleLow = Math.max(0.0, feasibleLow);
        feasibleHigh = Math.min(BoilerConstants.CAPACITY, feasibleHigh);
        boolean levelOutOfRange = levelReading < 0.0 || levelReading > BoilerConstants.CAPACITY;
        boolean levelImplausible = levelReading < feasibleLow || levelReading > feasibleHigh;
        if (!levelDevice.failed() && (levelOutOfRange || levelImplausible)) {
            levelDevice.detectFailure();
        }
        if (!levelDevice.failed()) {
            levelLowerBound = levelReading;
            levelUpperBound = levelReading;
        } else {
            levelLowerBound = feasibleLow;
            levelUpperBound = Math.max(feasibleLow, feasibleHigh);
        }
        levelEstimate = (levelLowerBound + levelUpperBound) / 2.0;

        // Extreme levels at the next cycle: no water supplied and steam rising
        // as fast as it can; or every pump that can pour pouring and steam
        // falling as fast as it can.
        worstCaseMinimumLevel = levelLowerBound - maximalSteamOutflow(steamUpperBound);
        worstCaseMaximumLevel = levelUpperBound + nextInflowMax - minimalSteamOutflow(steamLowerBound);

        historyKnown = true;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps[i].remember();
        }
    }

    /** Most steam (litre) that can leave in one cycle starting from a rate. */
    @RoboChartType("real")
    private static double maximalSteamOutflow(@RoboChartType("real") double rate) {
        double duration = BoilerConstants.CYCLE_DURATION;
        double gradient = BoilerConstants.STEAM_RISE_GRADIENT;
        double saturation = (BoilerConstants.MAX_STEAM - rate) / gradient;
        if (saturation <= 0.0) {
            return BoilerConstants.MAX_STEAM * duration;
        }
        if (saturation >= duration) {
            return rate * duration + gradient * duration * duration / 2.0;
        }
        return rate * saturation + gradient * saturation * saturation / 2.0
                + BoilerConstants.MAX_STEAM * (duration - saturation);
    }

    /** Least steam (litre) that can leave in one cycle starting from a rate. */
    @RoboChartType("real")
    private static double minimalSteamOutflow(@RoboChartType("real") double rate) {
        double duration = BoilerConstants.CYCLE_DURATION;
        double gradient = BoilerConstants.STEAM_FALL_GRADIENT;
        double exhaustion = rate / gradient;
        if (exhaustion >= duration) {
            return rate * duration - gradient * duration * duration / 2.0;
        }
        return rate * exhaustion / 2.0;
    }

    /**
     * The failure announcements and repair acknowledgements due this cycle.
     *
     * @return the messages, to be sent with the cycle's other messages
     */
    public List<OutputEvent> collectUnitStatusMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps[i].addStatusMessages(messages);
        }
        if (levelDevice.announcementDue()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamDevice.announcementDue()) {
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

    /** @return the number of successive transmissions that carried STOP */
    @RoboChartType("nat")
    public int successiveStops() {
        return successiveStops;
    }

    /** @return whether an indispensable message is missing or an aberrant one arrived */
    public boolean transmissionFailure() {
        return transmissionFailure;
    }

    /** @return whether STEAM_BOILER_WAITING has arrived, in this cycle or earlier */
    public boolean steamBoilerWaitingSeen() {
        return steamBoilerWaitingSeen;
    }

    /** @return whether STEAM_BOILER_WAITING arrived in this cycle */
    public boolean steamBoilerWaitingReceived() {
        return steamBoilerWaitingReceived;
    }

    /** @return whether PHYSICAL_UNITS_READY arrived in this cycle */
    public boolean physicalUnitsReadyReceived() {
        return physicalUnitsReadyReceived;
    }

    /** @return whether PROGRAM_READY was sent in an earlier cycle */
    public boolean programReadyAnnounced() {
        return programReadyAnnounced;
    }

    /** @return whether the evacuation valve is open */
    public boolean valveOpen() {
        return valveOpen;
    }

    /** @return q, the level most recently reported (litre) */
    @RoboChartType("real")
    public double levelReading() {
        return levelReading;
    }

    /** @return v, the steam rate most recently reported (litre/sec) */
    @RoboChartType("real")
    public double steamReading() {
        return steamReading;
    }

    /** @return p, the current throughput of the pumps together (litre/sec) */
    @RoboChartType("real")
    public double pumpThroughput() {
        return pumpThroughput;
    }

    /** @return the lower bound on the true water level (litre) */
    @RoboChartType("real")
    public double levelLowerBound() {
        return levelLowerBound;
    }

    /** @return the upper bound on the true water level (litre) */
    @RoboChartType("real")
    public double levelUpperBound() {
        return levelUpperBound;
    }

    /** @return the water level used for regulation: q, or the middle of the estimate (litre) */
    @RoboChartType("real")
    public double levelEstimate() {
        return levelEstimate;
    }

    /** @return the lowest level the next cycle can reach under worst-case behaviour (litre) */
    @RoboChartType("real")
    public double worstCaseMinimumLevel() {
        return worstCaseMinimumLevel;
    }

    /** @return the highest level the next cycle can reach under worst-case behaviour (litre) */
    @RoboChartType("real")
    public double worstCaseMaximumLevel() {
        return worstCaseMaximumLevel;
    }

    /** @return whether the level measuring device is out of order */
    public boolean levelDeviceFailed() {
        return levelDeviceFailed;
    }

    /** @return whether the steam measuring device is out of order */
    public boolean steamDeviceFailed() {
        return steamDeviceFailed;
    }

    /** @return whether some pump is out of order */
    public boolean pumpFailed() {
        return pumpFailed;
    }

    /** @return whether some pump controller is out of order */
    public boolean pumpControlFailed() {
        return pumpControlFailed;
    }
}
