package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/** Events received by the two controllers. */
public sealed interface InputEvent {

    /** A multi-sensor gas reading, ordered by sensing direction (CD-Evt1, CD-DM7). */
    record Gas(List<GasSample> reading) implements InputEvent {}

    /** An obstacle detected on the given side (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** The direction the robot should face next (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** The last reading showed no gas; keep searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
