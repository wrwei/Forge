package chemical_detector.actuator;

import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;

/**
 * Output of the gas-analysis subsystem: delivers turn, stop and resume to the
 * movement subsystem as they are sent, the only channel between the two
 * (CD-ARCH2). Records the last event sent.
 */
public final class AnalysisActuator {

    private final MovementController movement;
    private OutputEvent lastEvent;

    public AnalysisActuator(MovementController movement) {
        this.movement = movement;
    }

    /** Sends one event to the movement subsystem. */
    public void apply(OutputEvent output) {
        this.lastEvent = output;
        if (output instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) output;
            movement.step(new InputEvent.Turn(turn.direction()));
        } else if (output instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (output instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        }
    }

    /** The last event sent, or null before the first. */
    public OutputEvent lastEvent() {
        return lastEvent;
    }
}
