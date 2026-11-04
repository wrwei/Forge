package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the incoming transmission of the current cycle (SB-V3 first
 * stage, SB-DM4, SB-DM5, SB-DM6, SB-DM9), and its belief about the soundness of every
 * physical unit (SB-V9). Each cycle: {@link #beginTransmission()}, {@link #receive}
 * for every message, {@link #analyse}; after the controller's step,
 * {@link #recordOrders} and {@link #unitMessages()}. The query methods the
 * controller calls return values fixed by {@link #analyse}.
 */
public final class BoilerSensor {

    private final PumpUnit[] pumps = {new PumpUnit(), new PumpUnit(), new PumpUnit(), new PumpUnit()};
    private final UnitRecord levelUnit = new UnitRecord();
    private final UnitRecord steamUnit = new UnitRecord();

    @RoboChartType("nat") private int levelMessages;
    @RoboChartType("real") private double receivedLevel;
    @RoboChartType("nat") private int steamMessages;
    @RoboChartType("real") private double receivedSteam;
    private boolean stopReceived;
    private boolean waitingReceived;
    private boolean readyReceived;
    private boolean malformed;

    @RoboChartType("nat") private int stopRun;
    private boolean faultFound;
    private boolean levelOut;
    private boolean steamOut;
    private boolean pumpOut;
    private boolean controlOut;
    @RoboChartType("real") private double knownRate;
    @RoboChartType("real") private double possibleRate;

    /** Starts receiving a new incoming transmission. */
    public void beginTransmission() {
        levelUnit.beginTransmission();
        steamUnit.beginTransmission();
        for (PumpUnit unit : pumps) {
            unit.beginTransmission();
        }
        levelMessages = 0;
        receivedLevel = 0.0;
        steamMessages = 0;
        receivedSteam = 0.0;
        stopReceived = false;
        waitingReceived = false;
        readyReceived = false;
        malformed = false;
    }

    /** Records one message of the incoming transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopMessage) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            readyReceived = true;
        } else if (message instanceof InputEvent.LevelMessage) {
            InputEvent.LevelMessage level = (InputEvent.LevelMessage) message;
            levelMessages = levelMessages + 1;
            receivedLevel = level.litres();
        } else if (message instanceof InputEvent.SteamMessage) {
            InputEvent.SteamMessage steam = (InputEvent.SteamMessage) message;
            steamMessages = steamMessages + 1;
            receivedSteam = steam.rate();
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            if (validPump(state.reading().pumpIndex())) {
                pumps[state.reading().pumpIndex() - 1].receiveState(state.reading().pumpOn());
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState state = (InputEvent.PumpControlState) message;
            if (validPump(state.reading().controlIndex())) {
                pumps[state.reading().controlIndex() - 1].receiveControl(state.reading().waterFlowing());
            }
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired notice = (InputEvent.PumpRepaired) message;
            if (validPump(notice.pumpNumber())) {
                pumps[notice.pumpNumber() - 1].pump.noteRepaired();
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired notice = (InputEvent.PumpControlRepaired) message;
            if (validPump(notice.pumpNumber())) {
                pumps[notice.pumpNumber() - 1].control.noteRepaired();
            }
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement notice = (InputEvent.PumpFailureAcknowledgement) message;
            if (validPump(notice.pumpNumber())) {
                pumps[notice.pumpNumber() - 1].pump.noteAcknowledged();
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement notice =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            if (validPump(notice.pumpNumber())) {
                pumps[notice.pumpNumber() - 1].control.noteAcknowledged();
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelUnit.noteRepaired();
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamUnit.noteRepaired();
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelUnit.noteAcknowledged();
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamUnit.noteAcknowledged();
        } else {
            malformed = true;
        }
    }

    private boolean validPump(int number) {
        boolean valid = number >= 1 && number <= pumps.length;
        if (!valid) {
            malformed = true;
        }
        return valid;
    }

    /**
     * Analyses the received transmission. The prior bounds and steam rate are those
     * the controller held at the end of the previous cycle.
     */
    public void analyse(@RoboChartType("real") double priorLow, @RoboChartType("real") double priorHigh,
            @RoboChartType("real") double priorSteam) {
        boolean steamWasTrusted = steamUnit.sound();
        boolean levelConsistent = levelUnit.settleNotices();
        boolean steamConsistent = steamUnit.settleNotices();
        boolean consistent = levelConsistent && steamConsistent;
        boolean complete = levelMessages == 1 && steamMessages == 1;
        for (PumpUnit unit : pumps) {
            boolean unitConsistent = unit.settleNotices();
            consistent = consistent && unitConsistent;
            complete = complete && unit.complete();
        }
        // SB-FR5: an indispensable message missing, or a message whose presence is aberrant.
        faultFound = malformed || !complete || !consistent;
        // SB-V10: successive transmissions carrying STOP.
        if (stopReceived) {
            stopRun = stopRun + 1;
        } else {
            stopRun = 0;
        }
        if (!faultFound) {
            detectLevelFailure(priorLow, priorHigh, priorSteam, steamWasTrusted && steamUnit.sound());
            detectSteamFailure(priorSteam);
            for (PumpUnit unit : pumps) {
                unit.detectFailures();
            }
        }
        int known = 0;
        int possible = 0;
        boolean anyPump = false;
        boolean anyControl = false;
        for (PumpUnit unit : pumps) {
            if (unit.knownPouring()) {
                known = known + 1;
            }
            if (unit.mayPour()) {
                possible = possible + 1;
            }
            anyPump = anyPump || !unit.pump.sound();
            anyControl = anyControl || !unit.control.sound();
        }
        // SB-V7: throughput derived from pump and pump-controller states and P.
        knownRate = BoilerConstants.PUMP_CAPACITY_P * known;
        possibleRate = BoilerConstants.PUMP_CAPACITY_P * possible;
        levelOut = !levelUnit.sound();
        steamOut = !steamUnit.sound();
        pumpOut = anyPump;
        controlOut = anyControl;
    }

    /**
     * SB-FR3: a level outside [0, C], or outside what one cycle of extreme steam and
     * pump behaviour could reach from the prior bounds.
     */
    private void detectLevelFailure(double priorLow, double priorHigh, double priorSteam, boolean steamTrusted) {
        if (!levelUnit.sound()) {
            return;
        }
        double reachableLow;
        double reachableHigh;
        if (steamTrusted) {
            reachableLow = priorLow - BoilerConstants.CYCLE_SECONDS * priorSteam - BoilerConstants.STEAM_RISE_MARGIN;
            reachableHigh = priorHigh + BoilerConstants.PUMPS_MAX_INFLOW
                    - BoilerConstants.CYCLE_SECONDS * priorSteam + BoilerConstants.STEAM_FALL_MARGIN;
        } else {
            reachableLow = priorLow - BoilerConstants.STEAM_MAX_OUTFLOW;
            reachableHigh = priorHigh + BoilerConstants.PUMPS_MAX_INFLOW;
        }
        boolean outOfRange = receivedLevel < 0.0 || receivedLevel > BoilerConstants.CAPACITY_C;
        boolean unreachable = receivedLevel < reachableLow || receivedLevel > reachableHigh;
        if (outOfRange || unreachable) {
            levelUnit.detectFailure();
        }
    }

    /**
     * SB-FR4: a steam rate outside [0, W], or one that changed faster than U1 and U2
     * allow. The gradient check is skipped in the cycle the device reports its repair.
     */
    private void detectSteamFailure(double priorSteam) {
        if (!steamUnit.sound()) {
            return;
        }
        boolean outOfRange = receivedSteam < 0.0 || receivedSteam > BoilerConstants.STEAM_MAX_W;
        boolean tooFast = receivedSteam > priorSteam + BoilerConstants.STEAM_RISE_PER_CYCLE
                || receivedSteam < priorSteam - BoilerConstants.STEAM_FALL_PER_CYCLE;
        if (outOfRange || (tooFast && !steamUnit.repairAcknowledgementDue())) {
            steamUnit.detectFailure();
        }
    }

    /** Notes the pump orders the program sent in this cycle, for SB-FR1 and SB-FR2. */
    public void recordOrders(List<OutputEvent> sent) {
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                if (order.pumpNumber() >= 1 && order.pumpNumber() <= pumps.length) {
                    pumps[order.pumpNumber() - 1].recordOrder(true);
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                if (order.pumpNumber() >= 1 && order.pumpNumber() <= pumps.length) {
                    pumps[order.pumpNumber() - 1].recordOrder(false);
                }
            }
        }
    }

    /**
     * The messages due this cycle about unit soundness: every failure detection not yet
     * acknowledged (SB-FR6), and an acknowledgement of every repair announced in this
     * transmission (SB-FR7).
     */
    public List<OutputEvent> unitMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        if (levelUnit.announcementDue()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamUnit.announcementDue()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelUnit.repairAcknowledgementDue()) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamUnit.repairAcknowledgementDue()) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        for (int index = 0; index < pumps.length; index++) {
            int number = index + 1;
            if (pumps[index].pump.announcementDue()) {
                messages.add(new OutputEvent.PumpFailureDetection(number));
            }
            if (pumps[index].control.announcementDue()) {
                messages.add(new OutputEvent.PumpControlFailureDetection(number));
            }
            if (pumps[index].pump.repairAcknowledgementDue()) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(number));
            }
            if (pumps[index].control.repairAcknowledgementDue()) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(number));
            }
        }
        return messages;
    }

    /** LEVEL reading of this transmission (litre); 0 if none arrived. */
    @RoboChartType("real") public double levelValue() { return receivedLevel; }

    /** STEAM reading of this transmission (litre/sec); 0 if none arrived. */
    @RoboChartType("real") public double steamValue() { return receivedSteam; }

    /** Throughput of the pumps known to pour water (litre/sec). */
    @RoboChartType("real") public double flowingThroughput() { return knownRate; }

    /** Throughput of the pumps that may pour water, assuming the worst of unsound units (litre/sec). */
    @RoboChartType("real") public double openThroughput() { return possibleRate; }

    /** Number of successive transmissions, up to this one, that have carried STOP. */
    @RoboChartType("nat") public int successiveStopCount() { return stopRun; }

    /** Whether STEAM_BOILER_WAITING is in this transmission. */
    public boolean boilerWaitingPresent() { return waitingReceived; }

    /** Whether PHYSICAL_UNITS_READY is in this transmission. */
    public boolean unitsReadyPresent() { return readyReceived; }

    /** Whether this transmission shows a transmission failure (SB-FR5). */
    public boolean transmissionFault() { return faultFound; }

    /** Whether the level measuring device is out of order. */
    public boolean levelDefective() { return levelOut; }

    /** Whether the steam measuring device is out of order. */
    public boolean steamDefective() { return steamOut; }

    /** Whether some pump is out of order. */
    public boolean anyPumpDefective() { return pumpOut; }

    /** Whether some pump controller is out of order. */
    public boolean anyPumpControlDefective() { return controlOut; }
}
