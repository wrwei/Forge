package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.OutputEvent;

/**
 * The program's side of the message transmission system (SB-DM8): collects
 * the messages issued during one cycle, which are emitted together at the
 * cycle's transmit stage (SB-V3).
 */
public final class BoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private BoilerMode lastMode = BoilerMode.INITIALIZATION;

    /** Starts a new cycle: the previous cycle's messages have been transmitted. */
    public void beginCycle() {
        outgoing.clear();
    }

    /** Issues one message in the current cycle. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.ModeMessage) {
            OutputEvent.ModeMessage modeMessage = (OutputEvent.ModeMessage) message;
            lastMode = modeMessage.value();
        }
    }

    /** The messages issued in the current cycle, in the order they were issued. */
    public List<OutputEvent> outgoing() {
        return List.copyOf(outgoing);
    }

    /** The mode carried by the last MODE message issued. */
    public BoilerMode lastMode() {
        return lastMode;
    }
}
