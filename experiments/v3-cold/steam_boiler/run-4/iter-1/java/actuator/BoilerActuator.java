package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/**
 * Collects the messages of the current cycle's outgoing transmission (SB-DM8); all of
 * them are emitted together at the end of the cycle (SB-V3).
 */
public final class BoilerActuator {

    private final List<OutputEvent> pending = new ArrayList<>();

    /** Sends one message in the current outgoing transmission. */
    public void apply(OutputEvent message) {
        pending.add(message);
    }

    /** Starts a new outgoing transmission. */
    public void beginTransmission() {
        pending.clear();
    }

    /** The messages issued in the current outgoing transmission, in order. */
    public List<OutputEvent> outgoing() {
        return new ArrayList<>(pending);
    }
}
