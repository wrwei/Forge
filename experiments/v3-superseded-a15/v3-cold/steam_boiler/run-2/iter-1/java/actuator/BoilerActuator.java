package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/**
 * The outgoing side of the transmission system. Messages issued during a cycle are recorded in
 * order of issue and emitted together at the end of the cycle (SB-V3).
 */
public final class BoilerActuator {

    private final List<OutputEvent> pending = new ArrayList<>();

    /** Issues one outgoing message for the current cycle. */
    public void apply(OutputEvent message) {
        pending.add(message);
    }

    /** The messages issued so far in the current cycle, in order of issue. */
    public List<OutputEvent> pendingMessages() {
        return List.copyOf(pending);
    }

    /** Returns the messages issued in the cycle just ended and starts a new, empty cycle. */
    public List<OutputEvent> takePending() {
        List<OutputEvent> issued = List.copyOf(pending);
        pending.clear();
        return issued;
    }
}
