package chemdetector.actuator;

import java.util.ArrayList;
import java.util.List;

import chemdetector.event.MovementEvent;

/**
 * Carries the turn, stop, and resume events from the gas-analysis
 * subsystem to the movement subsystem (CD-ARCH2).
 */
public final class SignalPort {

    private final List<MovementEvent> pending = new ArrayList<>();

    public void send(MovementEvent event) {
        pending.add(event);
    }

    public List<MovementEvent> drain() {
        List<MovementEvent> out = new ArrayList<>(pending);
        pending.clear();
        return out;
    }
}
