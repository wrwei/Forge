package chemical_detector.actuator;

import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;

/**
 * The connection from the gas-analysis subsystem to the movement subsystem: the
 * turn, stop and resume events (CD-ARCH2).
 */
public final class MovementLinkActuator {

    private final MovementController movement;

    /** Creates the link delivering to the given movement subsystem. */
    public MovementLinkActuator(MovementController movement) {
        this.movement = movement;
    }

    /** Delivers a turn, stop or resume event to the movement subsystem. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Turn) {
            OutputEvent.Turn turnCommand = (OutputEvent.Turn) command;
            movement.step(new InputEvent.Turn(turnCommand.direction()));
        } else if (command instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (command instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        }
    }
}
