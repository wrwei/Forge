package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events sent by the controllers: turn, stop and resume from the gas-analysis
 * subsystem to the movement subsystem, and flag from the movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {

    /**
     * The direction of the strongest detected signal.
     *
     * @param direction the direction the robot should face next
     */
    record Turn(Angle direction) implements OutputEvent {
    }

    /** The chemical source has been confirmed: the robot must halt. */
    record Stop() implements OutputEvent {
    }

    /** The latest reading showed no target chemical: continue searching. */
    record Resume() implements OutputEvent {
    }

    /** Signals to the Vehicle that the chemical source has been located. */
    record Flag() implements OutputEvent {
    }
}
