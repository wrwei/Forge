package sranger.event;

/**
 * Input events of the SRanger controller (SR-DM3). All three are signals
 * without payload.
 */
public sealed interface InputEvent {

    /** The IR distance reading crossed below the obstacle-detection threshold. */
    record Obstacle() implements InputEvent {}

    /** One control cycle elapsed. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown: terminate the controller into Final. */
    record EndTask() implements InputEvent {}
}
