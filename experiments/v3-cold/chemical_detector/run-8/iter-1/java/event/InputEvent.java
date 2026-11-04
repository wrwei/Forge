package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;

import java.util.List;

/** Events received by the controllers: one record per event. */
public sealed interface InputEvent {
    /** gas (CD-Evt1): one multi-sensor gas reading, from the Vehicle to gas analysis. */
    record Gas(List<GasSample> reading) implements InputEvent {}

    /** obstacle (CD-Evt2): the side of a detected obstacle, from the Vehicle to movement. */
    record Obstacle(Loc side) implements InputEvent {}

    /** turn (CD-Evt4): the direction to face next, from gas analysis to movement. */
    record Turn(Angle direction) implements InputEvent {}

    /** stop (CD-Evt5): the chemical source is confirmed, from gas analysis to movement. */
    record Stop() implements InputEvent {}

    /** resume (CD-Evt6): keep searching, from gas analysis to movement. */
    record Resume() implements InputEvent {}
}
