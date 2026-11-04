package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.controller.OperatingMode;
import steam_boiler.event.OutputEvent;

/**
 * Collects the messages the controller emits in the current cycle for the
 * transmission stage (SB-V3, SB-DM8), and records the last mode issued.
 */
public final class BoilerActuator {

    private final List<OutputEvent> pendingMessages = new ArrayList<>();
    private OperatingMode lastModeIssued = OperatingMode.INITIALIZATION;

    /**
     * Issues one outgoing message of the current cycle.
     *
     * @param command the message to transmit
     */
    public void apply(OutputEvent command) {
        pendingMessages.add(command);
        if (command instanceof OutputEvent.Mode) {
            OutputEvent.Mode modeMessage = (OutputEvent.Mode) command;
            lastModeIssued = modeMessage.mode();
        }
    }

    /**
     * Hands over the messages issued since the last call, in issue order.
     *
     * @return the messages of the current cycle
     */
    public List<OutputEvent> takeMessages() {
        List<OutputEvent> messages = new ArrayList<>(pendingMessages);
        pendingMessages.clear();
        return messages;
    }

    /**
     * The mode carried by the last MODE message issued (SB-Beh24).
     *
     * @return the last mode issued
     */
    public OperatingMode lastModeIssued() {
        return lastModeIssued;
    }
}
