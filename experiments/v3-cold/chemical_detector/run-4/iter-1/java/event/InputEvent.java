package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events a controller receives: gas and obstacle from the Vehicle, and turn,
 * stop and resume from the gas-analysis subsystem to the movement subsystem.
 */
public sealed interface InputEvent {
    /** A multi-sensor gas reading, ordered by sensing direction. */
    record Gas(List<GasSensor> value) implements InputEvent {}

    /** An obstacle detected on the given side of the robot. */
    record Obstacle(Loc value) implements InputEvent {}

    /** The direction the robot should face next. */
    record Turn(Angle value) implements InputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {}

    /** A no-gas reading was analysed; continue searching. */
    record Resume() implements InputEvent {}
}
