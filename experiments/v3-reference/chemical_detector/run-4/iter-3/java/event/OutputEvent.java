package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * Events sent by the controllers: turn, stop and resume from the gas-analysis
 * subsystem to the movement subsystem; flag and the movement commands from the
 * movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {

    /** A command to face the given direction (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** The last reading showed no gas; keep searching (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {}

    /** Request for move(lv, direction) (CD-OP1). */
    record Move(Angle direction) implements OutputEvent {}

    /** Request for the halt command move(0, Front) (CD-OP1, CD-MV-FR3). */
    record Halt() implements OutputEvent {}

    /** Request for randomWalk() (CD-OP2). */
    record RandomWalk() implements OutputEvent {}

    /** Request for shortRandomWalk() (CD-OP3). */
    record ShortRandomWalk() implements OutputEvent {}

    /** Request for changeDirection(side) (CD-OP4). */
    record ChangeDirection(Loc side) implements OutputEvent {}
}
