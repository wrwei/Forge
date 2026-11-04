package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events the controllers send: turn, stop and resume from the gas-analysis
 * subsystem to the movement subsystem, and flag from the movement subsystem to
 * the Vehicle.
 */
public sealed interface OutputEvent {

    /** CD-Evt4: the direction of the strongest signal. */
    record Turn(Angle direction) implements OutputEvent {}

    /** CD-Evt5: the chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** CD-Evt6: the last reading had no gas. */
    record Resume() implements OutputEvent {}

    /** CD-Evt7: the chemical source has been located. */
    record Flag() implements OutputEvent {}
}
