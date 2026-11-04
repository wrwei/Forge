package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Carries the turn, stop and resume signals from the gas-analysis controller
 * to the movement controller. The gas-analysis controller emits at most one
 * signal per step, so the link holds at most one.
 */
public final class CommandLink {

    private OutputEvent pending;
    private boolean hasPending = false;

    /**
     * Receives a signal from the gas-analysis controller.
     */
    public void apply(OutputEvent signal) {
        this.pending = signal;
        this.hasPending = true;
    }

    /**
     * Whether a signal is waiting to be delivered.
     */
    public boolean hasPending() {
        return hasPending;
    }

    /**
     * Removes and returns the waiting signal; call only when {@link #hasPending()}.
     */
    public OutputEvent take() {
        this.hasPending = false;
        return pending;
    }
}
