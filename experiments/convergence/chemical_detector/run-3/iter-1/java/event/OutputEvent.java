package chemdetector.event;

import chemdetector.datamodel.Angle;

/**
 * Events emitted by the controllers. Turn, Stop and Resume are emitted
 * by the gas-analysis subsystem towards the movement subsystem; Flag is
 * emitted by the movement subsystem towards the Vehicle.
 */
public sealed interface OutputEvent {

    /** Steer towards the strongest detected signal. */
    record Turn(Angle direction) implements OutputEvent {
    }

    /** Chemical source confirmed — movement subsystem must halt. */
    record Stop() implements OutputEvent {
    }

    /** No gas detected — movement subsystem returns to searching. */
    record Resume() implements OutputEvent {
    }

    /** Chemical source located — signal the Vehicle platform. */
    record Flag() implements OutputEvent {
    }
}
