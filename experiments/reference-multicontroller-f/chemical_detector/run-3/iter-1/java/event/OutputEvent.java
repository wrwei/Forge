package chemical_detector.event;

import chemical_detector.types.Angle;

/**
 * Events a controller emits through its {@code Actuator}.
 */
public sealed interface OutputEvent {

    /** Steer towards the strongest signal. */
    record Turn(Angle angle) implements OutputEvent {}

    /** The chemical source has been found. */
    record Stop() implements OutputEvent {}

    /** Continue searching. */
    record Resume() implements OutputEvent {}

    /** Signal to the Vehicle that the chemical source has been located. */
    record Flag() implements OutputEvent {}
}
