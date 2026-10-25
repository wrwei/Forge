package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events emitted by the controllers. */
public sealed interface OutputEvent {

    /** Direction of the strongest signal, sent from gas analysis to movement. */
    record Turn(Angle angle) implements OutputEvent {}

    /** The chemical source has been confirmed, sent from gas analysis to movement. */
    record Stop() implements OutputEvent {}

    /** No gas in the last reading, sent from gas analysis to movement. */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located, sent from movement to the Vehicle. */
    record Flag() implements OutputEvent {}
}
