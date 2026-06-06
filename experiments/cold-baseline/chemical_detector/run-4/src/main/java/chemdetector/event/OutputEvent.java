package chemdetector.event;

import chemdetector.datatype.Angle;

/**
 * Output events emitted by the controllers towards the Vehicle or peer controller.
 * - Turn / Stop / Resume are shared between gas-analysis (emitter) and movement (receiver).
 * - Flag is emitted by movement to the Vehicle on entering Found.
 */
public sealed interface OutputEvent {
    record Turn(Angle a) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
