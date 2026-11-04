package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events a controller receives: gas and obstacle come from the Vehicle; turn,
 * stop and resume come from the gas-analysis controller to the movement
 * controller.
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading, ordered by sensing direction. */
    record Gas(List<GasSample> reading) implements InputEvent {}

    /** An obstacle has been detected on the given side. */
    record Obstacle(Loc side) implements InputEvent {}

    /** Face the given direction next. */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {}

    /** The last reading held no target chemical; keep searching. */
    record Resume() implements InputEvent {}
}
