package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/** Collects the output events a controller emits, in emission order. */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits an output event. */
    public void apply(OutputEvent out) {
        emitted.add(out);
    }

    /** Returns and clears the events emitted since the last drain. */
    public List<OutputEvent> drain() {
        var copy = List.copyOf(emitted);
        emitted.clear();
        return copy;
    }
}
