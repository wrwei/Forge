package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events emitted by the controllers: one record per event. */
public sealed interface OutputEvent {

    /** Gas analysis to movement: face the strongest detected signal. */
    record Turn(Angle direction) implements OutputEvent {}

    /** Gas analysis to movement: the chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** Gas analysis to movement: no target chemical in the last reading. */
    record Resume() implements OutputEvent {}

    /** Movement to Vehicle: the chemical source has been located. */
    record Flag() implements OutputEvent {}
}
