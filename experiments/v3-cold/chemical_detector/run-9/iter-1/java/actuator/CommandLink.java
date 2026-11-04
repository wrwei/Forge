package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/** Carries the gas-analysis commands (turn, stop, resume) to the movement subsystem; records the last one. */
public final class CommandLink {
    private OutputEvent lastSignal;
    private boolean issued = false;

    /** Receives a command emitted by gas analysis. */
    public void apply(OutputEvent signal) {
        this.lastSignal = signal;
        this.issued = true;
    }

    /** Whether any command has been issued. */
    public boolean issued() {
        return issued;
    }

    /** The last command issued; meaningful only once {@link #issued()} is true. */
    public OutputEvent lastSignal() {
        return lastSignal;
    }
}
