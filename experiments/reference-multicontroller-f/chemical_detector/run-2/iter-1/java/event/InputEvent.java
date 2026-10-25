package chemical_detector.event;

import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Loc;
import java.util.List;

/**
 * Events a controller receives: Vehicle sensor events (gas, obstacle), the inter-subsystem
 * signals (turn, stop, resume) and the periodic platform tick.
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

    /** The last reading showed no gas; continue searching. */
    record Resume() implements InputEvent {}

    /** Periodic platform tick; keeps terminal modes live. */
    record Tick() implements InputEvent {}
}
