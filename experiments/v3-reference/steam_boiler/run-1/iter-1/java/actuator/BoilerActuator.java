package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;

/**
 * Collects the messages the program emits during a cycle and transmits them
 * together at the end of it. Records the pump orders of the last
 * transmission, the commanded valve position and the last mode announced.
 */
public final class BoilerActuator {

    private static final int PUMPS = BoilerConstants.PUMP_COUNT;

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private final boolean[] openPending = new boolean[PUMPS];
    private final boolean[] closePending = new boolean[PUMPS];
    private final boolean[] openTransmitted = new boolean[PUMPS];
    private final boolean[] closeTransmitted = new boolean[PUMPS];
    private boolean valveOpen = false;
    private OperatingMode lastMode = OperatingMode.INITIALISATION_MODE;

    /** Adds a message to the outgoing transmission of the current cycle. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.OpenPump) {
            OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
            int index = order.pump() - 1;
            if (index >= 0 && index < PUMPS) {
                openPending[index] = true;
                closePending[index] = false;
            }
        } else if (message instanceof OutputEvent.ClosePump) {
            OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
            int index = order.pump() - 1;
            if (index >= 0 && index < PUMPS) {
                closePending[index] = true;
                openPending[index] = false;
            }
        } else if (message instanceof OutputEvent.Valve) {
            valveOpen = !valveOpen;
        } else if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode announced = (OutputEvent.Mode) message;
            lastMode = announced.value();
        }
    }

    /** Emits the current cycle's messages together and starts a new cycle. */
    public List<OutputEvent> transmit() {
        List<OutputEvent> sent = new ArrayList<>(outgoing);
        outgoing.clear();
        for (int index = 0; index < PUMPS; index++) {
            openTransmitted[index] = openPending[index];
            closeTransmitted[index] = closePending[index];
            openPending[index] = false;
            closePending[index] = false;
        }
        return sent;
    }

    /** True when the last transmission carried OPEN_PUMP for the given pump (1 to 4). */
    public boolean openOrdered(@RoboChartType("nat") int pump) {
        int index = pump - 1;
        return index >= 0 && index < PUMPS && openTransmitted[index];
    }

    /** True when the last transmission carried CLOSE_PUMP for the given pump (1 to 4). */
    public boolean closeOrdered(@RoboChartType("nat") int pump) {
        int index = pump - 1;
        return index >= 0 && index < PUMPS && closeTransmitted[index];
    }

    /** True while the evacuation valve has been commanded open. */
    public boolean valveOpened() {
        return valveOpen;
    }

    /** The mode carried by the last MODE message issued. */
    public OperatingMode lastMode() {
        return lastMode;
    }
}
