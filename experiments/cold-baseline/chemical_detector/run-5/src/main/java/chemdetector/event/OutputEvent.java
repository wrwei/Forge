package chemdetector.event;

/**
 * Boundary outputs emitted by the movement controller to the Vehicle.
 */
public sealed interface OutputEvent {

    /** CD-Evt7 — flag (signal) emitted on entry to Found. */
    record Flag() implements OutputEvent {
    }
}
