package sranger.event;

/**
 * Input events received by the SRanger controller.
 * Requirement: SR-DM3.
 *  - Obstacle: emitted by the IR sensor framework when the distance reading
 *    crosses below obstacleThreshold (signal, no payload).
 *  - Tick: delivered each control cycle by the system framework (signal).
 *  - EndTask: emitted by the operator-level shutdown channel (signal).
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
