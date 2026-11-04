package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.domain.FlowObservation;
import steam_boiler.domain.PumpControlReport;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.PumpStateReport;
import steam_boiler.event.InputEvent;

/**
 * The messages of one incoming transmission. All messages of a cycle are
 * considered received simultaneously; the transmission is cleared at the start
 * of every cycle and filled message by message by the transmission system.
 */
public final class IncomingTransmission {

    private static final int PUMPS = SteamBoilerConstants.PUMP_COUNT;

    private boolean stop;
    private boolean steamBoilerWaiting;
    private boolean physicalUnitsReady;
    private final boolean[] pumpStateReceived = new boolean[PUMPS];
    private final boolean[] pumpOn = new boolean[PUMPS];
    private final boolean[] pumpControlStateReceived = new boolean[PUMPS];
    private final boolean[] waterFlowing = new boolean[PUMPS];
    private boolean levelReceived;
    @RoboChartType("real") private double level;
    private boolean steamReceived;
    @RoboChartType("real") private double steam;
    private final boolean[] pumpRepaired = new boolean[PUMPS];
    private final boolean[] pumpControlRepaired = new boolean[PUMPS];
    private boolean levelRepaired;
    private boolean steamRepaired;
    private final boolean[] pumpFailureAcknowledged = new boolean[PUMPS];
    private final boolean[] pumpControlFailureAcknowledged = new boolean[PUMPS];
    private boolean levelFailureAcknowledged;
    private boolean steamFailureAcknowledged;
    private boolean malformed;

    /** Forgets the previous transmission; called at the start of every cycle. */
    public void clear() {
        stop = false;
        steamBoilerWaiting = false;
        physicalUnitsReady = false;
        for (int i = 0; i < PUMPS; i++) {
            pumpStateReceived[i] = false;
            pumpOn[i] = false;
            pumpControlStateReceived[i] = false;
            waterFlowing[i] = false;
            pumpRepaired[i] = false;
            pumpControlRepaired[i] = false;
            pumpFailureAcknowledged[i] = false;
            pumpControlFailureAcknowledged[i] = false;
        }
        levelReceived = false;
        level = 0.0;
        steamReceived = false;
        steam = 0.0;
        levelRepaired = false;
        steamRepaired = false;
        levelFailureAcknowledged = false;
        steamFailureAcknowledged = false;
        malformed = false;
    }

    /** Records one message of the current transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stop = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            steamBoilerWaiting = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            physicalUnitsReady = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState pumpState = (InputEvent.PumpState) message;
            receivePumpState(pumpState.report());
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState pumpControlState = (InputEvent.PumpControlState) message;
            receivePumpControlState(pumpControlState.report());
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level reading = (InputEvent.Level) message;
            malformed = malformed || levelReceived;
            levelReceived = true;
            level = reading.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam reading = (InputEvent.Steam) message;
            malformed = malformed || steamReceived;
            steamReceived = true;
            steam = reading.value();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            flagPump(pumpRepaired, repaired.pump());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            flagPump(pumpControlRepaired, repaired.pump());
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepaired = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepaired = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement acknowledgement =
                    (InputEvent.PumpFailureAcknowledgement) message;
            flagPump(pumpFailureAcknowledged, acknowledgement.pump());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement acknowledgement =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            flagPump(pumpControlFailureAcknowledged, acknowledgement.pump());
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelFailureAcknowledged = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamFailureAcknowledged = true;
        }
    }

    private void receivePumpState(PumpStateReport report) {
        int pump = report.pumpNumber();
        if (validPump(pump) && !pumpStateReceived[pump - 1]) {
            pumpStateReceived[pump - 1] = true;
            pumpOn[pump - 1] = report.reportedPosition() == PumpPosition.PUMP_ON;
        } else {
            malformed = true;
        }
    }

    private void receivePumpControlState(PumpControlReport report) {
        int pump = report.controlledPump();
        if (validPump(pump) && !pumpControlStateReceived[pump - 1]) {
            pumpControlStateReceived[pump - 1] = true;
            waterFlowing[pump - 1] = report.observedFlow() == FlowObservation.WATER_FLOWING;
        } else {
            malformed = true;
        }
    }

    private void flagPump(boolean[] flags, @RoboChartType("nat") int pump) {
        if (validPump(pump)) {
            flags[pump - 1] = true;
        } else {
            malformed = true;
        }
    }

    private static boolean validPump(@RoboChartType("nat") int pump) {
        return pump >= 1 && pump <= PUMPS;
    }

    boolean stopPresent() {
        return stop;
    }

    boolean steamBoilerWaitingPresent() {
        return steamBoilerWaiting;
    }

    boolean physicalUnitsReadyPresent() {
        return physicalUnitsReady;
    }

    // Every data message (pump states, controller states, level, steam) is present.
    boolean dataMessagesComplete() {
        boolean complete = levelReceived && steamReceived;
        for (int i = 0; i < PUMPS; i++) {
            complete = complete && pumpStateReceived[i] && pumpControlStateReceived[i];
        }
        return complete;
    }

    // A data message is duplicated, or a message names a pump that does not exist.
    boolean malformedMessagePresent() {
        return malformed;
    }

    boolean pumpOn(@RoboChartType("nat") int index) {
        return pumpOn[index];
    }

    boolean waterFlowing(@RoboChartType("nat") int index) {
        return waterFlowing[index];
    }

    @RoboChartType("real")
    double levelReading() {
        return level;
    }

    @RoboChartType("real")
    double steamReading() {
        return steam;
    }

    boolean pumpRepairedPresent(@RoboChartType("nat") int index) {
        return pumpRepaired[index];
    }

    boolean pumpControlRepairedPresent(@RoboChartType("nat") int index) {
        return pumpControlRepaired[index];
    }

    boolean levelRepairedPresent() {
        return levelRepaired;
    }

    boolean steamRepairedPresent() {
        return steamRepaired;
    }

    boolean pumpFailureAcknowledgedPresent(@RoboChartType("nat") int index) {
        return pumpFailureAcknowledged[index];
    }

    boolean pumpControlFailureAcknowledgedPresent(@RoboChartType("nat") int index) {
        return pumpControlFailureAcknowledged[index];
    }

    boolean levelFailureAcknowledgedPresent() {
        return levelFailureAcknowledged;
    }

    boolean steamFailureAcknowledgedPresent() {
        return steamFailureAcknowledged;
    }
}
