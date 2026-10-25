package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events received by the controllers: from the Vehicle (gas, obstacle, tick) and,
 * for the movement subsystem, from the gas-analysis subsystem (turn, stop, resume).
 */
public sealed interface InputEvent {
    /** A multi-sensor gas reading; list position encodes the sensing direction. */
    record Gas(List<GasSensor> gs) implements InputEvent {}

    /** An obstacle detected on side {@code l}. */
    record Obstacle(Loc l) implements InputEvent {}

    /** Direction the robot should face next. */
    record Turn(Angle a) implements InputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {}

    /** Analysis found no gas; continue searching. */
    record Resume() implements InputEvent {}

    /** Periodic control-cycle tick with no other payload. */
    record Tick() implements InputEvent {}
}
