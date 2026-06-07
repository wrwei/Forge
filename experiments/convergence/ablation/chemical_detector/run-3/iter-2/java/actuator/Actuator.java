package chemdetector.actuator;

import chemdetector.event.OutputEvent;

/** Receives output events emitted by the reasoning subsystems (CD-ARCH2). */
public final class Actuator {

    private OutputEvent lastOutput;

    public void apply(OutputEvent output) {
        this.lastOutput = output;
    }

    public OutputEvent lastOutput() {
        return lastOutput;
    }
}
