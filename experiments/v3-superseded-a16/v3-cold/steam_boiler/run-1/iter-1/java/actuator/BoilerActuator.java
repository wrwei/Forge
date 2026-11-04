package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/** Collects the messages the program sends in the current cycle and transmits them together. */
public final class BoilerActuator {
    private final List<OutputEvent> outgoing = new ArrayList<>();

    /** Adds a message to the current cycle's outgoing transmission. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
    }

    /** Last stage of a cycle: emits every message of the cycle at once and starts a new transmission. */
    public List<OutputEvent> transmit() {
        List<OutputEvent> sent = new ArrayList<>(outgoing);
        outgoing.clear();
        return sent;
    }
}
