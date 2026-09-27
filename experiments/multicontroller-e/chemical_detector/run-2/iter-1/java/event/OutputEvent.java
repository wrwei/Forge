package chemical_detector.event;

import chemical_detector.data.Angle;

/** Events a controller emits. */
public sealed interface OutputEvent {

    /** turn: steer towards the strongest signal (CD-Evt4). */
    record Turn(Angle angle) implements OutputEvent {
    }

    /** stop: the chemical source has been confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {
    }

    /** resume: no gas in the last reading, continue searching (CD-Evt6). */
    record Resume() implements OutputEvent {
    }

    /** flag: tells the Vehicle the source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {
    }
}
