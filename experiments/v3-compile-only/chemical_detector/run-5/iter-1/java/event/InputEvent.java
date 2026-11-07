package chemical_detector.event;

import chemical_detector.types.Angle;
import chemical_detector.types.GasSample;
import chemical_detector.types.Loc;
import java.util.List;

/**
 * Events a controller receives. Gas and Obstacle come from the Vehicle;
 * Turn, Stop and Resume are sent by the gas-analysis subsystem to the
 * movement subsystem.
 */
public sealed interface InputEvent {

    /** A multi-sensor gas reading (CD-Evt1). */
    record Gas(List<GasSample> reading) implements InputEvent {}

    /** An obstacle detected at the given side (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** A command to face the given direction (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** The last reading showed no gas; continue searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
