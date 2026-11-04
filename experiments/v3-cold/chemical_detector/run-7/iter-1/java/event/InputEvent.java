package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasMeasurement;
import chemical_detector.domain.Loc;
import java.util.List;

/** Events received by the controllers: one record per event. */
public sealed interface InputEvent {

    /** A multi-sensor gas reading from the Vehicle; list position (1-based) is the sensing direction. */
    record Gas(List<GasMeasurement> samples) implements InputEvent {}

    /** An obstacle detected by the Vehicle at the given side. */
    record Obstacle(Loc side) implements InputEvent {}

    /** The direction the robot should face next, sent by the gas-analysis subsystem. */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed, sent by the gas-analysis subsystem. */
    record Stop() implements InputEvent {}

    /** The last reading had no target chemical; continue searching. */
    record Resume() implements InputEvent {}
}
