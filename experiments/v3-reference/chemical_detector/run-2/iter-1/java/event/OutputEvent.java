package chemical_detector.event;

import chemical_detector.Angle;

/**
 * Events sent by the controllers: turn, stop and resume from the gas-analysis subsystem
 * to the movement subsystem, and flag from the movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {

    /** CD-Evt4: the direction of the strongest signal. */
    record Turn(Angle direction) implements OutputEvent {
    }

    /** CD-Evt5: the chemical source has been confirmed. */
    record Stop() implements OutputEvent {
    }

    /** CD-Evt6: the last reading showed no gas. */
    record Resume() implements OutputEvent {
    }

    /** CD-Evt7: the chemical source has been located; the platform may halt and report. */
    record Flag() implements OutputEvent {
    }
}
