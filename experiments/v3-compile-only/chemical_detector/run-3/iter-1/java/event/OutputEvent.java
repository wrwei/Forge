package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events emitted by the controllers. */
public sealed interface OutputEvent {
    /** Gas analysis asks movement to face the strongest signal (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** Gas analysis reports that the source has been found (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** Gas analysis asks movement to resume searching (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** Movement tells the Vehicle the source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
