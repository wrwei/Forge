package chemical_detector.event;

import chemical_detector.data.Angle;

/** Events emitted by a controller through its actuator. */
public sealed interface OutputEvent {

    /** turn: steer towards the strongest signal. */
    record Turn(Angle angle) implements OutputEvent {
    }

    /** stop: the chemical source has been confirmed. */
    record Stop() implements OutputEvent {
    }

    /** resume: return to searching. */
    record Resume() implements OutputEvent {
    }

    /** flag: tells the Vehicle the chemical source has been located. */
    record Flag() implements OutputEvent {
    }
}
