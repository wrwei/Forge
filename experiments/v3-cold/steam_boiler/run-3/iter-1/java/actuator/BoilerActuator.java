package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.OutputEvent;

/**
 * The outgoing transmission of the current cycle: collects the messages the
 * program sends, all emitted simultaneously at the end of the cycle, and
 * records the last mode announced.
 */
public final class BoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private OperatingMode lastMode = OperatingMode.INITIALIZATION;

    /** Adds a message to the current cycle's outgoing transmission. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode mode = (OutputEvent.Mode) message;
            lastMode = mode.value();
        }
    }

    /** The messages of the current cycle's outgoing transmission, in issue order. */
    public List<OutputEvent> outgoingMessages() {
        return new ArrayList<>(outgoing);
    }

    /** The mode carried by the last MODE(m) message sent. */
    public OperatingMode lastMode() {
        return lastMode;
    }

    /** Starts a new outgoing transmission. */
    public void clear() {
        outgoing.clear();
    }
}
