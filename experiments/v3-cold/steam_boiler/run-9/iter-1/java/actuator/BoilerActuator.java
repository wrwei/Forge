package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * Transmission stage of the program (SB-V3, SB-DM8). The controller's commands
 * are collected into the outgoing transmission of the current cycle; every
 * order is recorded with the sensor stage so that the next cycle's analysis can
 * judge the units' answers (SB-FR1, SB-FR2, SB-FR5). Pump and valve orders are
 * sent only when they change the commanded state.
 */
public final class BoilerActuator {

    private final BoilerSensor sensor;
    private final List<OutputEvent> outgoing = new ArrayList<>();
    private ValvePosition valve = ValvePosition.CLOSED;

    /** Creates the transmission stage that reports to the given sensor stage. */
    public BoilerActuator(BoilerSensor sensor) {
        this.sensor = sensor;
    }

    /** Starts the outgoing transmission of a new cycle. */
    public void beginTransmission() {
        outgoing.clear();
    }

    /** The messages of the current outgoing transmission, all emitted together. */
    public List<OutputEvent> transmission() {
        return new ArrayList<>(outgoing);
    }

    /** MODE(m): the current operating mode, sent on every cycle (SB-Beh24). */
    public void broadcastMode(BoilerMode mode) {
        outgoing.add(new OutputEvent.ModeMessage(mode));
        sensor.noteModeSent(mode);
    }

    /** PROGRAM_READY (SB-Beh7). */
    public void announceProgramReady() {
        outgoing.add(new OutputEvent.ProgramReady());
        sensor.noteProgramReadySent();
    }

    /** VALVE, to open the evacuation valve if it is closed (SB-Beh4). */
    public void commandValveOpen() {
        if (valve == ValvePosition.CLOSED) {
            outgoing.add(new OutputEvent.Valve());
            valve = ValvePosition.OPEN;
            sensor.noteValve(true);
        }
    }

    /** VALVE, to close the evacuation valve if it is open (SB-DM7). */
    public void commandValveClosed() {
        if (valve == ValvePosition.OPEN) {
            outgoing.add(new OutputEvent.Valve());
            valve = ValvePosition.CLOSED;
            sensor.noteValve(false);
        }
    }

    /** OPEN_PUMP(n), if pump n is not already commanded on. */
    public void commandPumpOn(@RoboChartType("nat") int pump) {
        if (!sensor.pumpCommandedOn(pump)) {
            outgoing.add(new OutputEvent.OpenPump(pump));
            sensor.notePumpOrder(pump, true);
        }
    }

    /** CLOSE_PUMP(n), if pump n is commanded on. */
    public void commandPumpOff(@RoboChartType("nat") int pump) {
        if (sensor.pumpCommandedOn(pump)) {
            outgoing.add(new OutputEvent.ClosePump(pump));
            sensor.notePumpOrder(pump, false);
        }
    }

    /** Failure detections not yet acknowledged and repair acknowledgements due (SB-FR6, SB-FR7). */
    public void reportUnitStatus() {
        List<OutputEvent> due = sensor.statusMessagesDue();
        for (int i = 0; i < due.size(); i++) {
            outgoing.add(due.get(i));
        }
    }

    /** The commanded position of the evacuation valve. */
    public ValvePosition valvePosition() {
        return valve;
    }
}
