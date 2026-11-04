package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events received by the controllers: gas and obstacle from the Vehicle, turn, stop
 * and resume from the gas-analysis subsystem to the movement subsystem.
 */
public sealed interface InputEvent {

    /** A multi-sensor gas reading (CD-Evt1). */
    record Gas(List<GasSample> reading) implements InputEvent {}

    /** An obstacle detected on the given side (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** A command to face the given direction (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** The last reading showed no gas; keep searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
