package chemical_detector.event;

import chemical_detector.types.Angle;

/**
 * Events a controller emits: the inter-subsystem signals (turn, stop, resume) sent by gas
 * analysis, and the flag sent by movement to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Direction of the strongest detected signal. */
    record Turn(Angle a) implements OutputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** The last reading showed no gas; continue searching. */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located. */
    record Flag() implements OutputEvent {}
}
