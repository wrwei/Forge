package sranger.event;

/**
 * Input events to the SRanger controller. Each is a signal event with no payload.
 * - Obstacle: emitted by the IR sensor when distance crosses below obstacleThreshold.
 * - Tick: delivered each control cycle by the system framework.
 * - EndTask: emitted by the operator shutdown channel to terminate into Final.
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
