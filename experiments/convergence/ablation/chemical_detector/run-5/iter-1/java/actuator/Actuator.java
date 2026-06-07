package chemdetector.actuator;

import chemdetector.event.OutputEvent;

/**
 * Receives output events emitted by the controllers and retains the most
 * recent one for inspection (CD-ARCH2).
 */
public final class Actuator {

    private OutputEvent lastSignal;

    public void apply(OutputEvent signal) {
        this.lastSignal = signal;
    }

    public OutputEvent lastSignal() {
        return lastSignal;
    }
}
