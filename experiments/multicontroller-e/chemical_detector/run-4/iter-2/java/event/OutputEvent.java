package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller emits: turn, stop and resume from gas analysis to movement,
 * and flag from movement to the Vehicle.
 */
public sealed interface OutputEvent {
    record Turn(Angle angle) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
