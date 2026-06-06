package chemdetector.event;

import chemdetector.datamodel.Angle;

/**
 * Events emitted by the controllers. turn, stop and resume are sent by
 * the gas-analysis subsystem to the movement subsystem; flag is sent by
 * the movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Steer towards the strongest detected signal. */
    record turn(Angle value) implements OutputEvent {
    }

    /** Chemical source confirmed; movement subsystem must halt. */
    record stop() implements OutputEvent {
    }

    /** Reading contained no target chemical; keep searching. */
    record resume() implements OutputEvent {
    }

    /** Chemical source located; platform may halt and report. */
    record flag() implements OutputEvent {
    }
}
