package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.event.OutputEvent;

/**
 * The sending side of the message transmission system: collects the outgoing
 * transmission of the current cycle and records the pump and valve orders.
 */
public final class BoilerActuator {
    @RoboChartType("nat") private static final int PUMPS = SteamBoilerConstants.PUMP_COUNT;

    private final List<OutputEvent> messages = new ArrayList<>();
    private final boolean[] opened = new boolean[PUMPS];
    private final boolean[] closed = new boolean[PUMPS];
    // VALVE requests opening, and the next VALVE requests closure.
    private boolean valveOpenCommanded = false;

    /** Adds a message to the outgoing transmission of this cycle. */
    public void apply(OutputEvent message) {
        messages.add(message);
        if (message instanceof OutputEvent.OpenPump) {
            OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
            if (validPump(order.pump())) {
                opened[order.pump()] = true;
                closed[order.pump()] = false;
            }
        } else if (message instanceof OutputEvent.ClosePump) {
            OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
            if (validPump(order.pump())) {
                closed[order.pump()] = true;
                opened[order.pump()] = false;
            }
        } else if (message instanceof OutputEvent.Valve) {
            valveOpenCommanded = !valveOpenCommanded;
        }
    }

    /** Starts a new outgoing transmission, forgetting the previous cycle's messages and orders. */
    public void beginCycle() {
        messages.clear();
        for (int n = 0; n < PUMPS; n++) {
            opened[n] = false;
            closed[n] = false;
        }
    }

    /** The messages of the outgoing transmission of this cycle. */
    public List<OutputEvent> outgoing() {
        return List.copyOf(messages);
    }

    /** Whether OPEN_PUMP(pump) was sent in this cycle. */
    public boolean openOrdered(@RoboChartType("nat") int pump) {
        return validPump(pump) && opened[pump];
    }

    /** Whether CLOSE_PUMP(pump) was sent in this cycle. */
    public boolean closeOrdered(@RoboChartType("nat") int pump) {
        return validPump(pump) && closed[pump];
    }

    /** Whether the VALVE messages sent so far leave the evacuation valve open. */
    public boolean valveCommandedOpen() {
        return valveOpenCommanded;
    }

    private static boolean validPump(int pump) {
        return pump >= 0 && pump < PUMPS;
    }
}
