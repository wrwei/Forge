package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/** Carries the gas-analysis subsystem's turn, stop and resume events to the movement subsystem. */
public final class CommandActuator {

    private OutputEvent lastCommand;

    /** Emits an event; the last one emitted is kept for delivery. */
    public void apply(OutputEvent command) {
        this.lastCommand = command;
    }

    /** The last event emitted, or null before the first. */
    public OutputEvent lastCommand() {
        return lastCommand;
    }
}
