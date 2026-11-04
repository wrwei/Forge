package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;
import steam_boiler.sensor.UnitStatus;

/**
 * The transmit stage (SB-V3): collects the messages the program sends in the
 * current cycle (SB-DM8) and emits them together, and records the last values
 * issued (mode, valve position, pump orders). Pump numbers run from 1 to 4.
 */
public final class BoilerActuator {

    private final BoilerSensor sensor;
    private final List<OutputEvent> outbox = new ArrayList<>();
    private final boolean[] pumpOrderedOn = new boolean[BoilerConstants.PUMP_COUNT];
    private ModeValue lastMode = ModeValue.INITIALIZATION_MODE;
    private ValvePosition valve = ValvePosition.VALVE_CLOSED;

    /** Creates the actuator; the sensor tells it which pumps are sound and running. */
    public BoilerActuator(BoilerSensor sensor) {
        this.sensor = sensor;
    }

    /** Adds a message to the current transmission. */
    public void apply(OutputEvent message) {
        outbox.add(message);
        if (message instanceof OutputEvent.ModeMessage) {
            OutputEvent.ModeMessage mode = (OutputEvent.ModeMessage) message;
            lastMode = mode.value();
        } else if (message instanceof OutputEvent.OpenPump) {
            OutputEvent.OpenPump open = (OutputEvent.OpenPump) message;
            recordPumpOrder(open.pump(), true);
        } else if (message instanceof OutputEvent.ClosePump) {
            OutputEvent.ClosePump close = (OutputEvent.ClosePump) message;
            recordPumpOrder(close.pump(), false);
        }
    }

    /**
     * Requests a position of the evacuation valve (SB-DM7, SB-Beh4); VALVE is
     * sent only when the requested position differs from the current one.
     */
    public void setValve(ValvePosition position) {
        if (position != valve) {
            valve = position;
            outbox.add(new OutputEvent.Valve());
        }
    }

    /**
     * Switches sound pumps on or off so that {@code targetCount} of them run
     * (SB-Beh5, SB-Beh10): OPEN_PUMP for the lowest-numbered sound pumps that
     * are off, CLOSE_PUMP for the highest-numbered sound pumps that are on.
     */
    public void regulatePumps(@RoboChartType("nat") int targetCount) {
        int running = 0;
        for (int pump = 1; pump <= BoilerConstants.PUMP_COUNT; pump++) {
            if (sensor.pumpUsable(pump) && sensor.pumpReportedOn(pump)) {
                running = running + 1;
            }
        }
        for (int pump = 1; pump <= BoilerConstants.PUMP_COUNT && running < targetCount; pump++) {
            if (sensor.pumpUsable(pump) && !sensor.pumpReportedOn(pump)) {
                apply(new OutputEvent.OpenPump(pump));
                running = running + 1;
            }
        }
        for (int pump = BoilerConstants.PUMP_COUNT; pump >= 1 && running > targetCount; pump--) {
            if (sensor.pumpUsable(pump) && sensor.pumpReportedOn(pump)) {
                apply(new OutputEvent.ClosePump(pump));
                running = running - 1;
            }
        }
    }

    /**
     * Adds the unit-protocol messages of this cycle: every failure detection
     * not yet acknowledged is repeated (SB-FR6), and every repair received in
     * this cycle is acknowledged (SB-FR7).
     */
    public void announceUnitStatus() {
        for (int pump = 1; pump <= BoilerConstants.PUMP_COUNT; pump++) {
            UnitStatus pumpUnit = sensor.pumpStatus(pump);
            UnitStatus controlUnit = sensor.pumpControlStatus(pump);
            if (pumpUnit.detectionPending()) {
                outbox.add(new OutputEvent.PumpFailureDetection(pump));
            }
            if (pumpUnit.repairAcknowledgementDue()) {
                outbox.add(new OutputEvent.PumpRepairedAcknowledgement(pump));
            }
            if (controlUnit.detectionPending()) {
                outbox.add(new OutputEvent.PumpControlFailureDetection(pump));
            }
            if (controlUnit.repairAcknowledgementDue()) {
                outbox.add(new OutputEvent.PumpControlRepairedAcknowledgement(pump));
            }
        }
        if (sensor.levelStatus().detectionPending()) {
            outbox.add(new OutputEvent.LevelFailureDetection());
        }
        if (sensor.levelStatus().repairAcknowledgementDue()) {
            outbox.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (sensor.steamStatus().detectionPending()) {
            outbox.add(new OutputEvent.SteamFailureDetection());
        }
        if (sensor.steamStatus().repairAcknowledgementDue()) {
            outbox.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
    }

    /** Emits every message of the current cycle at once and starts an empty transmission. */
    public List<OutputEvent> transmit() {
        List<OutputEvent> sent = new ArrayList<>(outbox);
        outbox.clear();
        return sent;
    }

    /** The mode most recently sent with MODE(m). */
    public ModeValue lastMode() {
        return lastMode;
    }

    /** The position most recently requested for the evacuation valve. */
    public ValvePosition valvePosition() {
        return valve;
    }

    /** True when the last order sent to pump {@code pump} was OPEN_PUMP. */
    public boolean pumpOrderedOn(@RoboChartType("nat") int pump) {
        return pump >= 1 && pump <= BoilerConstants.PUMP_COUNT && pumpOrderedOn[pump - 1];
    }

    private void recordPumpOrder(int pump, boolean switchedOn) {
        if (pump >= 1 && pump <= BoilerConstants.PUMP_COUNT) {
            pumpOrderedOn[pump - 1] = switchedOn;
        }
    }
}
