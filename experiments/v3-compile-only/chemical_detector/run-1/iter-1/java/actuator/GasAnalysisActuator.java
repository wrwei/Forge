package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Events the gas-analysis subsystem sends to the movement subsystem
 * (CD-Evt4..6). Keeps the last event and the one not yet forwarded.
 */
public final class GasAnalysisActuator {

    private OutputEvent lastEvent = null;
    private OutputEvent pending = null;

    /** Emits an event towards the movement subsystem. */
    public void apply(OutputEvent output) {
        this.lastEvent = output;
        this.pending = output;
    }

    /** The last event emitted, or null if none yet. */
    public OutputEvent lastEvent() {
        return lastEvent;
    }

    /** Returns and clears the event not yet forwarded, or null if none. */
    public OutputEvent takePending() {
        OutputEvent taken = pending;
        this.pending = null;
        return taken;
    }
}
