package chemical_detector.actuator;

import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;

/**
 * Receives the gas-analysis subsystem's output events (turn, stop, resume) and delivers
 * them to the movement subsystem, the only channel between the two (CD-ARCH2).
 */
public final class GasAnalysisActuator {

    private final MovementController movement;

    /**
     * @param movement the movement subsystem that consumes turn, stop and resume
     */
    public GasAnalysisActuator(MovementController movement) {
        this.movement = movement;
    }

    /** Delivers one turn, stop or resume event to the movement subsystem. */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Turn) {
            OutputEvent.Turn turnSignal = (OutputEvent.Turn) signal;
            movement.step(new InputEvent.Turn(turnSignal.direction()));
        } else if (signal instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (signal instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        }
    }
}
