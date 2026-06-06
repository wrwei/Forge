package sranger.event;

/**
 * SR-DM3: signal-only input events received by the SRanger controller.
 *
 *  - Obstacle: emitted by the IR sensor framework when distance drops below
 *              the obstacle-detection threshold.
 *  - Tick:     framework heartbeat delivered each control cycle.
 *  - EndTask:  operator-level shutdown command terminating the controller.
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
