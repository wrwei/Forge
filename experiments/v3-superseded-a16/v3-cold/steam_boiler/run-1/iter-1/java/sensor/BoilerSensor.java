package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.SteamBoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.data.PumpControllerReading;
import steam_boiler.data.PumpReading;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the plant. Each cycle it receives one transmission, analyses it
 * (transmission check, STOP counting, failure detection, repairs and acknowledgements,
 * level estimation) and exposes the results to the controller as cached quantities.
 */
public final class BoilerSensor {
    /** Allowance for rounding when a reading is compared with a predicted bound. */
    @RoboChartType("real") private static final double ROUNDING_ALLOWANCE = 0.001;

    private final List<PumpMonitor> pumps = new ArrayList<>();
    private final UnitStatus levelDevice = new UnitStatus();
    private final UnitStatus steamDevice = new UnitStatus();

    // Messages of the current transmission
    private boolean stopReceived;
    private boolean steamBoilerWaitingPresent;
    private boolean physicalUnitsReadyPresent;
    private boolean levelRepairReceived;
    private boolean steamRepairReceived;
    private boolean levelAcknowledgementReceived;
    private boolean steamAcknowledgementReceived;
    private boolean aberrantMessage;
    @RoboChartType("nat") private int levelMessages;
    @RoboChartType("nat") private int steamMessages;
    @RoboChartType("real") private double levelReading;
    @RoboChartType("real") private double steamReading;

    // History kept from one cycle to the next
    private boolean started;
    private boolean historyAvailable;
    private boolean valveOpen;
    private boolean previousSteamKnown;
    @RoboChartType("real") private double previousSteam;

    // Results of the analysis, read by the controller
    @RoboChartType("nat") private int stopCount;
    private boolean transmissionFailure;
    private boolean levelFailure;
    private boolean steamFailure;
    private boolean pumpFailure;
    private boolean pumpControllerFailure;
    @RoboChartType("real") private double throughput;
    @RoboChartType("real") private double lowerBound;
    @RoboChartType("real") private double upperBound;
    @RoboChartType("real") private double lowestNext;
    @RoboChartType("real") private double highestNext;

    /** Creates the view of a plant with {@link SteamBoilerConstants#PUMP_COUNT} pumps. */
    public BoilerSensor() {
        for (int number = 1; number <= SteamBoilerConstants.PUMP_COUNT; number++) {
            pumps.add(new PumpMonitor(number));
        }
    }

    /** First stage of a cycle: records the messages of one incoming transmission. */
    public void receive(List<InputEvent> transmission) {
        stopReceived = false;
        steamBoilerWaitingPresent = false;
        physicalUnitsReadyPresent = false;
        levelRepairReceived = false;
        steamRepairReceived = false;
        levelAcknowledgementReceived = false;
        steamAcknowledgementReceived = false;
        aberrantMessage = false;
        levelMessages = 0;
        steamMessages = 0;
        levelDevice.beginCycle();
        steamDevice.beginCycle();
        for (PumpMonitor pump : pumps) {
            pump.beginCycle();
        }
        for (InputEvent message : transmission) {
            record(message);
        }
    }

    private void record(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            steamBoilerWaitingPresent = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            physicalUnitsReadyPresent = true;
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            levelReading = level.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamReading = steam.value();
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState pumpState = (InputEvent.PumpState) message;
            PumpReading reading = pumpState.reading();
            if (validPump(reading.pump())) {
                pumpNumbered(reading.pump()).recordState(reading.open());
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState controlState = (InputEvent.PumpControlState) message;
            PumpControllerReading reading = controlState.reading();
            if (validPump(reading.pump())) {
                pumpNumbered(reading.pump()).recordControlState(reading.flowing());
            }
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            if (validPump(repaired.pump())) {
                pumpNumbered(repaired.pump()).recordPumpRepaired();
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            if (validPump(repaired.pump())) {
                pumpNumbered(repaired.pump()).recordControlRepaired();
            }
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement acknowledgement = (InputEvent.PumpFailureAcknowledgement) message;
            if (validPump(acknowledgement.pump())) {
                pumpNumbered(acknowledgement.pump()).recordPumpAcknowledgement();
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement acknowledgement =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            if (validPump(acknowledgement.pump())) {
                pumpNumbered(acknowledgement.pump()).recordControlAcknowledgement();
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairReceived = true;
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAcknowledgementReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAcknowledgementReceived = true;
        }
    }

    /** A pump number outside 1..4 makes the message aberrant (SB-FR5). */
    private boolean validPump(@RoboChartType("nat") int number) {
        if (number < 1 || number > SteamBoilerConstants.PUMP_COUNT) {
            aberrantMessage = true;
            return false;
        }
        return true;
    }

    private PumpMonitor pumpNumbered(@RoboChartType("nat") int number) {
        return pumps.get(number - 1);
    }

    /**
     * Second stage of a cycle, before the controller decides: checks the transmission,
     * counts STOP messages, applies acknowledgements and repairs, detects unit failures and
     * advances the level estimate. Failure detection starts with STEAM_BOILER_WAITING.
     */
    public void analyse() {
        if (steamBoilerWaitingPresent) {
            started = true;
        }
        if (stopReceived) {
            stopCount = stopCount + 1;
        } else {
            stopCount = 0;
        }

        boolean failure = aberrantMessage || levelMessages != 1 || steamMessages != 1;
        for (PumpMonitor pump : pumps) {
            if (!pump.dataComplete()) {
                failure = true;
            }
        }
        if (levelAcknowledgementReceived && !levelDevice.acknowledgeFailure()) {
            failure = true;
        }
        if (steamAcknowledgementReceived && !steamDevice.acknowledgeFailure()) {
            failure = true;
        }
        if (levelRepairReceived && !levelDevice.repair()) {
            failure = true;
        }
        if (steamRepairReceived && !steamDevice.repair()) {
            failure = true;
        }
        for (PumpMonitor pump : pumps) {
            if (!pump.applyAcknowledgementsAndRepairs()) {
                failure = true;
            }
        }
        transmissionFailure = failure;

        boolean judging = started && !transmissionFailure;
        if (judging && historyAvailable) {
            for (PumpMonitor pump : pumps) {
                pump.detectFailures();
            }
        }
        if (judging) {
            checkSteam();
            checkLevel();
        }
        estimateLevel();
        commitHistory();
    }

    /** SB-FR4: the steam rate must lie in [0, W] and change no faster than U1 and U2 allow. */
    private void checkSteam() {
        if (!steamDevice.isFailed()) {
            boolean valid = steamReading >= 0.0 && steamReading <= SteamBoilerConstants.STEAM_MAX_W;
            if (valid && historyAvailable && previousSteamKnown && !steamDevice.wasRepairedThisCycle()) {
                double duration = SteamBoilerConstants.CYCLE_DURATION;
                double lowest = previousSteam - SteamBoilerConstants.STEAM_DECREASE_U2 * duration;
                double highest = previousSteam + SteamBoilerConstants.STEAM_INCREASE_U1 * duration;
                valid = steamReading >= lowest - ROUNDING_ALLOWANCE && steamReading <= highest + ROUNDING_ALLOWANCE;
            }
            if (!valid) {
                steamDevice.detectFailure();
            }
        }
    }

    /**
     * SB-FR3: the level must lie in [0, C] and be reachable from the previous cycle under
     * extreme behaviour of steam and pumps (and of the evacuation valve, while it is open).
     */
    private void checkLevel() {
        if (!levelDevice.isFailed()) {
            boolean valid = levelReading >= 0.0 && levelReading <= SteamBoilerConstants.CAPACITY_C;
            if (valid && historyAvailable && !levelDevice.wasRepairedThisCycle()) {
                double perCycle = SteamBoilerConstants.PUMP_CAPACITY_P * SteamBoilerConstants.CYCLE_DURATION;
                double extremeInflow = 0.0;
                for (PumpMonitor pump : pumps) {
                    extremeInflow = extremeInflow + pump.extremeInflow(perCycle);
                }
                double lowest = lowerBound - maximumSteamOutflow(previousSteamKnown, previousSteam);
                double highest = upperBound - minimumSteamOutflow(previousSteamKnown, previousSteam) + extremeInflow;
                if (valveOpen) {
                    lowest = 0.0;
                }
                valid = levelReading >= lowest - ROUNDING_ALLOWANCE && levelReading <= highest + ROUNDING_ALLOWANCE;
            }
            if (!valid) {
                levelDevice.detectFailure();
            }
        }
    }

    /**
     * SB-V8: while the level device works the estimate is the reading itself; while it is out
     * of order the bounds are advanced from the steam rate and the water the pumps delivered,
     * one litre pumped counting as one litre of boiler content. Also derives p (SB-V7) and
     * the worst-case level one cycle ahead (SB-Beh13, SB-Beh18, SB-Beh21, SB-Beh25).
     */
    private void estimateLevel() {
        double perCycle = SteamBoilerConstants.PUMP_CAPACITY_P * SteamBoilerConstants.CYCLE_DURATION;
        if (!levelDevice.isFailed()) {
            lowerBound = levelReading;
            upperBound = levelReading;
        } else if (historyAvailable) {
            double minimumInflow = 0.0;
            double maximumInflow = 0.0;
            for (PumpMonitor pump : pumps) {
                minimumInflow = minimumInflow + pump.minimumInflow(perCycle);
                maximumInflow = maximumInflow + pump.maximumInflow(perCycle);
            }
            double lowest = lowerBound - maximumSteamOutflow(previousSteamKnown, previousSteam) + minimumInflow;
            double highest = upperBound - minimumSteamOutflow(previousSteamKnown, previousSteam) + maximumInflow;
            if (valveOpen) {
                lowest = 0.0;
            }
            lowerBound = Math.max(0.0, lowest);
            upperBound = Math.min(SteamBoilerConstants.CAPACITY_C, highest);
        } else {
            lowerBound = 0.0;
            upperBound = SteamBoilerConstants.CAPACITY_C;
        }

        double pouringPumps = 0.0;
        double inflowNext = 0.0;
        for (PumpMonitor pump : pumps) {
            if (pump.pouring()) {
                pouringPumps = pouringPumps + 1.0;
            }
            inflowNext = inflowNext + pump.maximumInflowNextCycle(perCycle);
        }
        throughput = pouringPumps * SteamBoilerConstants.PUMP_CAPACITY_P;

        boolean steamKnown = !steamDevice.isFailed();
        lowestNext = lowerBound - maximumSteamOutflow(steamKnown, steamReading);
        highestNext = upperBound - minimumSteamOutflow(steamKnown, steamReading) + inflowNext;

        levelFailure = levelDevice.isFailed();
        steamFailure = steamDevice.isFailed();
        pumpFailure = false;
        pumpControllerFailure = false;
        for (PumpMonitor pump : pumps) {
            if (pump.pumpFailed()) {
                pumpFailure = true;
            }
            if (pump.controllerFailed()) {
                pumpControllerFailure = true;
            }
        }
    }

    /** Most steam that can leave during one cycle starting at rate {@code rate} (litre). */
    @RoboChartType("real")
    private static double maximumSteamOutflow(boolean rateKnown, @RoboChartType("real") double rate) {
        double duration = SteamBoilerConstants.CYCLE_DURATION;
        double ceiling = SteamBoilerConstants.STEAM_MAX_W * duration;
        if (!rateKnown) {
            return ceiling;
        }
        double rising = rate * duration + 0.5 * SteamBoilerConstants.STEAM_INCREASE_U1 * duration * duration;
        return Math.min(ceiling, rising);
    }

    /** Least steam that can leave during one cycle starting at rate {@code rate} (litre). */
    @RoboChartType("real")
    private static double minimumSteamOutflow(boolean rateKnown, @RoboChartType("real") double rate) {
        if (!rateKnown) {
            return 0.0;
        }
        double duration = SteamBoilerConstants.CYCLE_DURATION;
        double falling = rate * duration - 0.5 * SteamBoilerConstants.STEAM_DECREASE_U2 * duration * duration;
        return Math.max(0.0, falling);
    }

    private void commitHistory() {
        previousSteamKnown = !steamDevice.isFailed();
        previousSteam = steamReading;
        for (PumpMonitor pump : pumps) {
            pump.commitHistory();
        }
        if (started) {
            historyAvailable = true;
        }
    }

    /**
     * The failure announcements still awaiting acknowledgement (SB-FR6) and the
     * acknowledgements of the repairs received in this cycle (SB-FR7).
     */
    public List<OutputEvent> statusMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (PumpMonitor pump : pumps) {
            pump.addStatusMessages(messages);
        }
        if (levelDevice.isAwaitingAcknowledgement()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamDevice.isAwaitingAcknowledgement()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelDevice.wasRepairedThisCycle()) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamDevice.wasRepairedThisCycle()) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return messages;
    }

    /** Last stage of a cycle: notes the pump and valve orders the program has just sent. */
    public void recordTransmission(List<OutputEvent> sent) {
        for (PumpMonitor pump : pumps) {
            pump.clearOrder();
        }
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                if (validPump(order.pump())) {
                    pumpNumbered(order.pump()).order(true);
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                if (validPump(order.pump())) {
                    pumpNumbered(order.pump()).order(false);
                }
            } else if (message instanceof OutputEvent.Valve) {
                valveOpen = !valveOpen;
            }
        }
    }

    /** SB-V10: the number of successive transmissions that carried STOP. */
    @RoboChartType("nat")
    public int successiveStops() {
        return stopCount;
    }

    /** SB-FR5: an indispensable message was missing or a message was aberrant. */
    public boolean transmissionFailed() {
        return transmissionFailure;
    }

    /** STEAM_BOILER_WAITING arrived in this transmission. */
    public boolean steamBoilerWaitingReceived() {
        return steamBoilerWaitingPresent;
    }

    /** PHYSICAL_UNITS_READY arrived in this transmission. */
    public boolean physicalUnitsReadyReceived() {
        return physicalUnitsReadyPresent;
    }

    /** SB-V9: the level measuring device is out of order. */
    public boolean levelDeviceFailed() {
        return levelFailure;
    }

    /** SB-V9: the steam measuring device is out of order. */
    public boolean steamDeviceFailed() {
        return steamFailure;
    }

    /** SB-V9: at least one pump is out of order. */
    public boolean anyPumpFailed() {
        return pumpFailure;
    }

    /** SB-V9: at least one pump controller is out of order. */
    public boolean anyPumpControllerFailed() {
        return pumpControllerFailure;
    }

    /** SB-V5: q, the water level last reported (litre). */
    @RoboChartType("real")
    public double waterLevel() {
        return levelReading;
    }

    /** SB-V6: v, the steam rate last reported (litre/sec). */
    @RoboChartType("real")
    public double steamRate() {
        return steamReading;
    }

    /** SB-V7: p, the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return throughput;
    }

    /** SB-V8: lower bound of the water level (litre). */
    @RoboChartType("real")
    public double levelLowerBound() {
        return lowerBound;
    }

    /** SB-V8: upper bound of the water level (litre). */
    @RoboChartType("real")
    public double levelUpperBound() {
        return upperBound;
    }

    /** Lowest level the boiler can reach by the next cycle under worst-case behaviour (litre). */
    @RoboChartType("real")
    public double lowestLevelNextCycle() {
        return lowestNext;
    }

    /** Highest level the boiler can reach by the next cycle under worst-case behaviour (litre). */
    @RoboChartType("real")
    public double highestLevelNextCycle() {
        return highestNext;
    }
}
