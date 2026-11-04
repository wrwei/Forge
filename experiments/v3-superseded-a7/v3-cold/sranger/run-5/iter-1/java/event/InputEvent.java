package sranger.event;

/**
 * Input events of the SRanger controller (SR-DM3). All three are signals
 * without payload.
 */
public sealed interface InputEvent {

    /** The IR distance reading crossed below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown: terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
