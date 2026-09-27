package sranger.event;

/**
 * Input events delivered to the SRanger controller (SR-DM3). All three are
 * signal events and carry no payload.
 */
public sealed interface InputEvent {

    /** Raised by the IR sensor framework when the distance crosses below the threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Raised by the operator-level shutdown channel. */
    record EndTask() implements InputEvent {}
}
