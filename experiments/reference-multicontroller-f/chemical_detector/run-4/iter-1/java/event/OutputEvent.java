package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events emitted by the controllers: turn, stop and resume from the gas-analysis
 * subsystem to the movement subsystem, and flag from the movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {
    /** Direction of the strongest detected signal. */
    record Turn(Angle a) implements OutputEvent {}

    /** The chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** Analysis found no gas; continue searching. */
    record Resume() implements OutputEvent {}

    /** Signals to the Vehicle that the chemical source has been located. */
    record Flag() implements OutputEvent {}
}
