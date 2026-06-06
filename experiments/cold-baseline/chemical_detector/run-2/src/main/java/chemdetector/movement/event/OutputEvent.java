package chemdetector.movement.event;

/**
 * Events emitted by the movement subsystem to the Vehicle. CD-Evt7.
 */
public sealed interface OutputEvent {

    /** CD-Evt7: tell the Vehicle the chemical source has been found. */
    record Flag() implements OutputEvent {}
}
