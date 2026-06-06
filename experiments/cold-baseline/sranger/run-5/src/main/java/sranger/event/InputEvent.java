package sranger.event;

/**
 * Input events to the SRanger controller.
 * - Obstacle: emitted by the IR sensor framework when distance crosses below obstacleThreshold.
 * - Tick:     delivered each control cycle by the system framework.
 * - EndTask:  emitted by the operator-level shutdown channel; terminates into Final.
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
