package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Output port of the gas-analysis subsystem: receives the turn, stop and resume
 * events it sends to the movement subsystem (CD-Evt4..6) and records the last one.
 */
public final class GasAnalysisActuator {

    private OutputEvent lastOutput = new OutputEvent.Resume();

    /** Sends an event to the movement subsystem. */
    public void apply(OutputEvent output) {
        this.lastOutput = output;
    }

    /** The last event sent. */
    public OutputEvent lastOutput() {
        return lastOutput;
    }
}
