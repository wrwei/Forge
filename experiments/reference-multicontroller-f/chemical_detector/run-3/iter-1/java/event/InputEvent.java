package chemical_detector.event;

import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Loc;
import java.util.List;

/**
 * Events a controller's {@code step} consumes.
 */
public sealed interface InputEvent {

    /** A multi-sensor gas reading; list position encodes the sensing direction. */
    record Gas(List<GasSensor> readings) implements InputEvent {}

    /** An obstacle detected on the given side. */
    record Obstacle(Loc side) implements InputEvent {}

    /** Direction the robot should face next, sent by the gas-analysis subsystem. */
    record Turn(Angle angle) implements InputEvent {}

    /** Chemical source confirmed, sent by the gas-analysis subsystem. */
    record Stop() implements InputEvent {}

    /** No gas in the last reading, sent by the gas-analysis subsystem. */
    record Resume() implements InputEvent {}

    /** Passage of one time unit with no other event. */
    record Tick() implements InputEvent {}
}
