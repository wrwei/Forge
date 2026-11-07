package sranger.event;

/**
 * Input events of the SRanger controller (SR-DM3). All three are signals
 * without payload.
 */
public sealed interface InputEvent {

    /** Emitted by the IR sensor framework when the distance reading crosses below obstacleThreshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered each control cycle by the system framework. */
    record Tick() implements InputEvent {}

    /** Emitted by the operator-level shutdown channel; terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
