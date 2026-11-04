package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/**
 * Collects the messages the program emits in the transmit stage of a cycle (SB-V3, SB-DM8).
 */
public final class BoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();

    /**
     * Queues one outgoing message for this cycle's transmission.
     *
     * @param message the message to send
     */
    public void apply(OutputEvent message) {
        outgoing.add(message);
    }

    /** Starts a new cycle with no outgoing message. */
    public void clear() {
        outgoing.clear();
    }

    /** @return the messages queued in this cycle, in the order they were issued. */
    public List<OutputEvent> outgoing() {
        return new ArrayList<>(outgoing);
    }
}
