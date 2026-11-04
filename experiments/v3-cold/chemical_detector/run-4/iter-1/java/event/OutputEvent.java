package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller sends: turn, stop and resume from the gas-analysis
 * subsystem to the movement subsystem, and flag from the movement subsystem
 * to the Vehicle.
 */
public sealed interface OutputEvent {
    /** The direction the robot should face next. */
    record Turn(Angle value) implements OutputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** A no-gas reading was analysed; continue searching. */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located. */
    record Flag() implements OutputEvent {}
}
