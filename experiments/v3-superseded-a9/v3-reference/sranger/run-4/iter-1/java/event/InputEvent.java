package sranger.event;

/**
 * Input events of the SRanger controller. All three are signals without a
 * payload.
 */
public sealed interface InputEvent {

    /** Emitted by the IR sensor framework when the distance crosses below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered once per control cycle by the system framework. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown: terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
