package chemical_detector.actuator;

import chemical_detector.controller.MovementController;
import chemical_detector.event.MovementEvent;

/**
 * The only link between the two reasoning subsystems: carries the turn, stop and
 * resume commands emitted by gas analysis to the movement subsystem, synchronously.
 */
public final class MovementCommandChannel {

    private MovementController receiver;
    private MovementEvent lastDelivered;

    public void connect(MovementController movement) {
        this.receiver = movement;
    }

    public void deliver(MovementEvent command) {
        this.lastDelivered = command;
        if (receiver != null) {
            receiver.step(command);
        }
    }

    public MovementEvent lastDelivered() {
        return lastDelivered;
    }
}
