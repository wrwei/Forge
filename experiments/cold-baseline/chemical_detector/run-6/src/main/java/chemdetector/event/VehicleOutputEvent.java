package chemdetector.event;

/**
 * Output events emitted to the Vehicle by the movement controller.
 */
public sealed interface VehicleOutputEvent {

    /** CD-Evt7 — chemical source located; halt and report. */
    record Flag() implements VehicleOutputEvent {}
}
