package chemical_detector.system;

import chemical_detector.actuator.OutputPort;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;

/**
 * Delivers the turn, stop and resume events emitted by the gas-analysis
 * subsystem to the movement subsystem, which is their only consumer.
 */
public final class MovementLink implements OutputPort {

    private final MovementController movement;

    public MovementLink(MovementController movement) {
        this.movement = movement;
    }

    @Override
    public void send(OutputEvent outputEvent) {
        if (outputEvent instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) outputEvent;
            movement.step(new InputEvent.Turn(turn.angle()));
        } else if (outputEvent instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (outputEvent instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        }
    }
}
