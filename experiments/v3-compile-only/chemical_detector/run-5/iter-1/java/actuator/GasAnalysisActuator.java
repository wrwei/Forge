package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Receives the gas-analysis subsystem's commands to the movement subsystem
 * (turn, stop, resume) and records the last one issued.
 */
public final class GasAnalysisActuator {

    private OutputEvent lastOutput;

    /** Issues a command to the movement subsystem. */
    public void apply(OutputEvent command) {
        this.lastOutput = command;
    }

    /** The last command issued, or null before the first. */
    public OutputEvent lastOutput() {
        return lastOutput;
    }
}
