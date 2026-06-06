package chemdetector.event;

import chemdetector.datamodel.Loc;

/**
 * Commands emitted by the movement subsystem to the Vehicle.
 */
public sealed interface VehicleEvent {

    /** Steer away from the obstacle on the given side (CD-OP4). */
    record ChangeDirection(Loc value) implements VehicleEvent {
    }
}
