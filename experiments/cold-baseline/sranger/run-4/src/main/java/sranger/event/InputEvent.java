package sranger.event;

/**
 * Input events received by the SRanger controller (SR-DM3).
 *  - obstacle: emitted by the IR sensor framework when distance &lt;= obstacleThreshold.
 *  - tick: control-cycle tick from the system framework.
 *  - endTask: operator-level shutdown signal.
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
