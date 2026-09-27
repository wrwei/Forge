package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Output port of a controller: holds the events it emits until they are drained. */
public final class Actuator {

    private final List<OutputEvent> pending = new ArrayList<>();

    /** Emits {@code out}. */
    public void apply(OutputEvent out) {
        pending.add(Objects.requireNonNull(out));
    }

    /** Returns the events emitted since the last drain, in order, and forgets them. */
    public List<OutputEvent> drain() {
        var drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }
}
