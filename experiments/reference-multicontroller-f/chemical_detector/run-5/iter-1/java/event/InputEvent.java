package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Loc;
import java.util.List;

/** Events received by a controller's step method. */
public sealed interface InputEvent {

    /** gas: one multi-sensor gas reading from the Vehicle. */
    record Gas(List<GasSensor> readings) implements InputEvent {
    }

    /** obstacle: side on which the Vehicle detected an obstacle. */
    record Obstacle(Loc side) implements InputEvent {
    }

    /** turn: direction the robot should face next, sent by the gas-analysis subsystem. */
    record Turn(Angle angle) implements InputEvent {
    }

    /** stop: the chemical source has been confirmed. */
    record Stop() implements InputEvent {
    }

    /** resume: the last reading showed no gas; continue searching. */
    record Resume() implements InputEvent {
    }

    /** tick: periodic platform heartbeat that keeps terminal modes live. */
    record Tick() implements InputEvent {
    }
}
