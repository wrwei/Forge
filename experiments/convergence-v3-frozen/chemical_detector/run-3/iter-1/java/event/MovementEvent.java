package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * Inputs of the movement subsystem: turn, stop and resume come from the
 * gas-analysis subsystem; obstacle comes from the Vehicle.
 */
public sealed interface MovementEvent {

    /** Face the given direction next. */
    record Turn(Angle direction) implements MovementEvent {
    }

    /** The chemical source has been confirmed. */
    record Stop() implements MovementEvent {
    }

    /** The last reading showed no gas: continue searching. */
    record Resume() implements MovementEvent {
    }

    /** An obstacle was detected on the given side. */
    record Obstacle(Loc side) implements MovementEvent {
    }

    /** A control cycle in which no movement event arrived. */
    record NoCommand() implements MovementEvent {
    }
}
