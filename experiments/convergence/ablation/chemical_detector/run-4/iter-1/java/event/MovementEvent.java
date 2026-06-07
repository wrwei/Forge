package chemdetector.event;

import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * Events consumed by the movement subsystem. {@code Turn}, {@code Stop}
 * and {@code Resume} are emitted by the gas-analysis subsystem;
 * {@code Obstacle} is emitted by the Vehicle's obstacle detector.
 */
public sealed interface MovementEvent {

    /** The direction the robot should face next. */
    record Turn(Angle dir) implements MovementEvent {
    }

    /** The chemical source has been confirmed; halt. */
    record Stop() implements MovementEvent {
    }

    /** A no-gas reading was analysed; return to searching. */
    record Resume() implements MovementEvent {
    }

    /** An obstacle has been detected on the given side. */
    record Obstacle(Loc side) implements MovementEvent {
    }
}
