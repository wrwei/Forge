package chemdetector.event;

import chemdetector.datatype.Angle;

/**
 * Sealed interface of all events emitted by the controllers
 * (towards other controllers or towards the Vehicle).
 */
public sealed interface OutputEvent {
    record Turn(Angle payload) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
