package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;

import java.util.List;

/** Events received by the controllers. */
public sealed interface InputEvent {
    /** A multi-sensor gas reading from the Vehicle (CD-Evt1). */
    record Gas(List<GasSensor> reading) implements InputEvent {}

    /** An obstacle detected at the given side (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** Direction the robot should face next, from gas analysis (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** The last reading had no target chemical; keep searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
