package chemdetector.actuator;

import chemdetector.controller.MovementController;
import chemdetector.event.MovementEvent;

/**
 * The communication channel from the gas-analysis subsystem to the
 * movement subsystem. The two reasoning subsystems communicate only via
 * the turn, stop, and resume events carried here.
 */
public final class AnalysisRelay {

    private MovementController movement;

    /** Connects the relay to the movement subsystem it delivers to. */
    public void connect(MovementController controller) {
        this.movement = controller;
    }

    /** Delivers one event to the movement subsystem. */
    public void apply(MovementEvent command) {
        if (movement != null) {
            movement.step(command);
        }
    }
}
