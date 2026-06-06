package chemdetector.event;

/**
 * Events emitted to the Vehicle platform.
 */
public sealed interface OutputEvent {

    /** Chemical source located; the platform may halt and report (CD-Evt7). */
    record Flag() implements OutputEvent {
    }
}
