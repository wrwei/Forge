package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events emitted by the controllers: one record per event. */
public sealed interface OutputEvent {

    /** Gas analysis tells movement which direction to face (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** Gas analysis tells movement that the source is found (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** Gas analysis tells movement to return to searching (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** Movement tells the Vehicle that the source is located (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
