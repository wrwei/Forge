package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * Input events consumed by the movement subsystem: turn, stop and resume come from
 * the gas-analysis subsystem; obstacle comes from the Vehicle.
 */
public sealed interface MovementEvent {

    /** Direction the robot should face next. */
    record Turn(Angle direction) implements MovementEvent {
    }

    /** The chemical source has been confirmed. */
    record Stop() implements MovementEvent {
    }

    /** The last reading showed no gas: resume searching. */
    record Resume() implements MovementEvent {
    }

    /** An obstacle has been detected on the given side. */
    record Obstacle(Loc side) implements MovementEvent {
    }
}
