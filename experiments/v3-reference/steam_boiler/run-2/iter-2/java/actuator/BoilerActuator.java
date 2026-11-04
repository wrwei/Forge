package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;

import steam_boiler.event.OutputEvent;

/**
 * The program's side of the message transmission system: collects the
 * messages issued during a cycle, which are emitted together as that cycle's
 * outgoing transmission (SB-V3).
 */
public final class BoilerActuator {

    private final List<OutputEvent> transmission = new ArrayList<>();

    /** Issues one outgoing message in the current cycle. */
    public void apply(OutputEvent message) {
        transmission.add(message);
    }

    /** Starts a new outgoing transmission. */
    public void beginTransmission() {
        transmission.clear();
    }

    /** The messages issued so far in the current cycle, in issue order. */
    public List<OutputEvent> sentThisCycle() {
        return new ArrayList<>(transmission);
    }
}
