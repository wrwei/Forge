package sranger.event;

/**
 * Input events of the SRanger controller. All three are signals with no payload.
 */
public sealed interface InputEvent {
    /** Emitted by the IR sensor framework when the distance crosses below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown; terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
