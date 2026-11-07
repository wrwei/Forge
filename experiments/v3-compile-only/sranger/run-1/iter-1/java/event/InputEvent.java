package sranger.event;

/**
 * Input events of the SRanger controller (SR-DM3). All three are signals
 * without a payload.
 */
public sealed interface InputEvent {

    /** Emitted by the IR sensor framework when the distance reading crosses below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Emitted by the operator-level shutdown channel; terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
