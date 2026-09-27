package sranger.event;

/**
 * Input events received by the SRanger controller.
 */
public sealed interface InputEvent {

    /** IR distance reading crossed below the obstacle-detection threshold. */
    record Obstacle() implements InputEvent {}

    /** Control-cycle tick from the system framework. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown request. */
    record EndTask() implements InputEvent {}
}
