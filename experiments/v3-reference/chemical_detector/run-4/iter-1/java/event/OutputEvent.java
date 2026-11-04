package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events sent by the controllers: turn, stop and resume from the gas-analysis
 * subsystem to the movement subsystem, and flag from the movement subsystem to the
 * Vehicle.
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
}
