package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.Message;
import steam_boiler.event.OutputEvent;
import steam_boiler.event.ProtocolMessage;

/**
 * The program's side of the message transmission system (SB-DM8): collects
 * the messages issued during one cycle, which are emitted together at the
 * cycle's transmit stage (SB-V3).
 */
public final class BoilerActuator {

    private final List<Message> outgoing = new ArrayList<>();
    private BoilerMode lastMode = BoilerMode.INITIALIZATION;

    /** Starts a new cycle: the previous cycle's messages have been transmitted. */
    public void beginCycle() {
        outgoing.clear();
    }

    /** Issues one of the controller's commands in the current cycle. */
    public void apply(OutputEvent command) {
        outgoing.add(command);
        if (command instanceof OutputEvent.ModeMessage) {
            OutputEvent.ModeMessage modeMessage = (OutputEvent.ModeMessage) command;
            lastMode = modeMessage.value();
        }
    }

    /** Issues one failure/repair protocol message in the current cycle (SB-FR6, SB-FR7). */
    public void announce(ProtocolMessage message) {
        outgoing.add(message);
    }

    /** The messages issued in the current cycle, in the order they were issued. */
    public List<Message> outgoing() {
        return List.copyOf(outgoing);
    }

    /** The mode carried by the last MODE message issued. */
    public BoilerMode lastMode() {
        return lastMode;
    }
}
