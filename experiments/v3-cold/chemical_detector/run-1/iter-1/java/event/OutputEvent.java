package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller emits: turn, stop and resume are sent by the
 * gas-analysis controller to the movement controller; flag is sent by the
 * movement controller to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Face the given direction next (the strongest detected signal). */
    record Turn(Angle direction) implements OutputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** The last reading held no target chemical; keep searching. */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located. */
    record Flag() implements OutputEvent {}
}
