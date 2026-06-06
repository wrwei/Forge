package chemdetector.actuator;

import chemdetector.event.OutputEvent;

/**
 * Output sink for events emitted by the subsystem state machines
 * (turn/stop/resume from gas-analysis, flag from movement). Each
 * {@code apply(new OutputEvent.X(...))} call is extracted by the M2M as
 * a RoboChart event communication.
 */
public final class Actuator {

    private OutputEvent last;

    public void apply(OutputEvent signal) {
        this.last = signal;
    }

    public OutputEvent last() {
        return last;
    }
}
