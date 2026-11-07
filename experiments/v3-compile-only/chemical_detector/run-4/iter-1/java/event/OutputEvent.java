package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller sends. The gas-analysis subsystem sends {@code Turn},
 * {@code Stop} and {@code Resume} to the movement subsystem (each matches the
 * {@link InputEvent} record of the same name); the movement subsystem sends
 * {@code Flag} to the Vehicle.
 */
public sealed interface OutputEvent {

    /** turn: the direction of the strongest signal (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** stop: the chemical source has been confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** resume: the last reading showed no gas (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** flag: the chemical source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
