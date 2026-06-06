package chemdetector.event;

/**
 * Output events emitted by the movement controller to the Vehicle.
 */
public sealed interface MovementOutputEvent {

    /** CD-Evt7. Flag — source located, signal Vehicle to halt and report. */
    record Flag() implements MovementOutputEvent {
    }
}
