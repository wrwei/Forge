package chemdetector.event;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * Events consumed by the movement subsystem. Turn, Stop, and Resume are
 * emitted by the gas-analysis subsystem (inter-controller events);
 * Obstacle is emitted by the Vehicle.
 */
public sealed interface MovementEvent {

    /** Direction the robot should face next (CD-Evt4). */
    record Turn(Angle value) implements MovementEvent {
    }

    /** Chemical source confirmed; halt (CD-Evt5). */
    record Stop() implements MovementEvent {
    }

    /** No-gas reading analysed; continue searching (CD-Evt6). */
    record Resume() implements MovementEvent {
    }

    /** Obstacle detected at the given side (CD-Evt2). */
    record Obstacle(Loc value) implements MovementEvent {
    }
}
