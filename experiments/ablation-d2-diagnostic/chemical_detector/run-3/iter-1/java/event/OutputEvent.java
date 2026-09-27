package chemical_detector.event;

import chemical_detector.data.Angle;

/** Events emitted by a controller through its {@link chemical_detector.actuator.Actuator}. */
public sealed interface OutputEvent {

    /** Gas analysis asks movement to face direction {@code a}. */
    record Turn(Angle a) implements OutputEvent {}

    /** Gas analysis reports that the chemical source has been confirmed. */
    record Stop() implements OutputEvent {}

    /** Gas analysis reports a no-gas reading; movement should resume searching. */
    record Resume() implements OutputEvent {}

    /** Movement signals to the Vehicle that the chemical source has been located. */
    record Flag() implements OutputEvent {}
}
