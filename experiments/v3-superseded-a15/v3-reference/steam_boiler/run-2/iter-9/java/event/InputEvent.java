package steam_boiler.event;

/**
 * The event that drives one control cycle. All incoming messages of a cycle are considered
 * received simultaneously; they are handed to the sensor, and the controller is then stepped
 * once with {@link MessagesReceived}.
 */
public sealed interface InputEvent {
    /** The cycle's incoming transmission has been received and analysed. */
    record MessagesReceived() implements InputEvent {}
}
