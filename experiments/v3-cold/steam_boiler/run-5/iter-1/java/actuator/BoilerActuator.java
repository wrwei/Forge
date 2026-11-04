package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/**
 * Receives the controller's outgoing messages and records those issued in the current cycle,
 * in the order issued; they are transmitted together at the end of the cycle.
 */
public final class BoilerActuator {

    private final List<OutputEvent> issuedMessages = new ArrayList<>();

    /** Issues one outgoing message in the current cycle. */
    public void apply(OutputEvent message) {
        issuedMessages.add(message);
    }

    /** The messages issued since the last {@link #clear()}. */
    public List<OutputEvent> issued() {
        return new ArrayList<>(issuedMessages);
    }

    /** Starts a new cycle with no message issued. */
    public void clear() {
        issuedMessages.clear();
    }
}
