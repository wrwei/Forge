package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.OutputEvent;

/**
 * Outbound event port of one controller. A controller emits at most one event per
 * step; it is held until the owner takes it.
 */
public final class Actuator {

    private OutputEvent pending;
    private OutputEvent last;
    @RoboChartType("nat")
    private int emitted;

    /** Emits {@code output}. */
    public void apply(OutputEvent output) {
        this.pending = output;
        this.last = output;
        this.emitted = emitted + 1;
    }

    public boolean hasPending() {
        return pending != null;
    }

    /** Removes and returns the pending event; call only when {@link #hasPending()}. */
    public OutputEvent take() {
        var output = pending;
        pending = null;
        return output;
    }

    /** The most recently emitted event, or {@code null} if none has been emitted. */
    public OutputEvent last() {
        return last;
    }

    @RoboChartType("nat")
    public int emitted() {
        return emitted;
    }
}
