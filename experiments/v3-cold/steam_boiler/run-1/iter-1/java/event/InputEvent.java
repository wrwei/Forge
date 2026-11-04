package steam_boiler.event;

/**
 * The event that drives the controller: one per five-second cycle.
 *
 * <p>All messages of a cycle are considered received simultaneously, so the
 * controller is stepped once per cycle, after the transmission sensor has
 * taken in the whole incoming transmission.
 */
public sealed interface InputEvent {
    /** The incoming transmission of the current cycle has been received. */
    record Transmission() implements InputEvent {}
}
