package sranger.event;

/**
 * Input events received by the SRanger controller.
 */
public sealed interface InputEvent {
    /** IR framework signal: the distance reading crossed below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** Control-cycle signal from the system framework. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown signal. */
    record EndTask() implements InputEvent {}
}
