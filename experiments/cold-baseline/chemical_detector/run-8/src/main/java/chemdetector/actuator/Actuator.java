package chemdetector.actuator;

import java.util.ArrayList;
import java.util.List;
import chemdetector.event.OutputEvent;

/**
 * Actuator: receives output events emitted by controllers.
 * Maintains a queue that a host harness can drain.
 */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    public void apply(OutputEvent e) {
        this.emitted.add(e);
    }

    public List<OutputEvent> emitted() {
        return emitted;
    }

    public void clear() {
        this.emitted.clear();
    }
}
