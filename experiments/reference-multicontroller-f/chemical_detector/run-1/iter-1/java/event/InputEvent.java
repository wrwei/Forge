package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/** Events received by the controllers. */
public sealed interface InputEvent {

    /** A multi-sensor gas reading emitted by the Vehicle. */
    record Gas(List<GasSensor> reading) implements InputEvent {}

    /** An obstacle detected by the Vehicle on the given side. */
    record Obstacle(Loc side) implements InputEvent {}

    /** Direction the robot should face next, sent by the gas-analysis subsystem. */
    record Turn(Angle angle) implements InputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {}

    /** The last reading showed no gas; continue searching. */
    record Resume() implements InputEvent {}
}
