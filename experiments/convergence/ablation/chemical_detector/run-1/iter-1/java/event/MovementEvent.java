package chemdetector.event;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/** Events consumed by the movement subsystem. */
public sealed interface MovementEvent {

    /** Direction the robot should face next (from the gas analysis). */
    record Turn(Angle direction) implements MovementEvent {
    }

    /** Chemical source confirmed: halt. */
    record Stop() implements MovementEvent {
    }

    /** No-gas reading analysed: return to searching. */
    record Resume() implements MovementEvent {
    }

    /** Obstacle encountered on the carried side. */
    record Obstacle(Loc side) implements MovementEvent {
    }
}
