package chemical_detector.event;

import chemical_detector.types.Angle;

/** Events emitted by the controllers. */
public sealed interface OutputEvent {
    /** Direction command from gas analysis to movement. */
    record turn(Angle a) implements OutputEvent {
    }

    /** Chemical source confirmed; sent from gas analysis to movement. */
    record stop() implements OutputEvent {
    }

    /** No gas in the last reading; sent from gas analysis to movement. */
    record resume() implements OutputEvent {
    }

    /** Chemical source located; sent from movement to the Vehicle. */
    record flag() implements OutputEvent {
    }
}
