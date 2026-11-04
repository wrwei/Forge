package steam_boiler.actuator;

import steam_boiler.OutgoingMessages;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.OutputEvent;

/**
 * Receives the program's commands to the physical units (SB-DM8) and records
 * the last mode announced; the commands go into the cycle's outgoing
 * transmission (SB-V3).
 */
public final class BoilerActuator {

    private final OutgoingMessages transmission;
    private BoilerMode lastMode = BoilerMode.INITIALIZATION;

    /** Creates the actuator over the cycle's outgoing transmission. */
    public BoilerActuator(OutgoingMessages transmission) {
        this.transmission = transmission;
    }

    /** Issues one command in the current cycle. */
    public void apply(OutputEvent command) {
        transmission.add(command);
        if (command instanceof OutputEvent.ModeMessage) {
            OutputEvent.ModeMessage modeMessage = (OutputEvent.ModeMessage) command;
            lastMode = modeMessage.value();
        }
    }

    /** The mode carried by the last MODE message issued. */
    public BoilerMode lastMode() {
        return lastMode;
    }
}
