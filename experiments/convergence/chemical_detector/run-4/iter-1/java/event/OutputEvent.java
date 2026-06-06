package chemdetector.event;

import chemdetector.datamodel.Angle;

/**
 * Events emitted by the controllers. Turn, Stop, Resume are sent by the
 * gas-analysis subsystem to the movement subsystem; Flag is sent by the
 * movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Direction the robot should face next. */
    record Turn(Angle payload) implements OutputEvent {
    }

    /** Chemical source confirmed: halt. */
    record Stop() implements OutputEvent {
    }

    /** No-gas reading analysed: continue searching. */
    record Resume() implements OutputEvent {
    }

    /** Chemical source located: platform may halt and report. */
    record Flag() implements OutputEvent {
    }
}
