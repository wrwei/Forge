package sranger.event;

/**
 * Input events received by the SRanger controller (SR-DM3). All are signals without payload.
 */
public sealed interface InputEvent {
    /** Emitted by the IR sensor framework when the distance crosses below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered each control cycle by the system framework. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown request. */
    record EndTask() implements InputEvent {}
}
