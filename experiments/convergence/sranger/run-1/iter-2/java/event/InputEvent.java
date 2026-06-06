package sranger.event;

/**
 * Input events delivered to the SRanger controller (SR-DM3).
 * All three are signal events carrying no payload.
 */
public sealed interface InputEvent {

    /** Emitted by the IR sensor framework when the distance crosses below the threshold. */
    record Obstacle() implements InputEvent {
    }

    /** Delivered each control cycle by the system framework. */
    record Tick() implements InputEvent {
    }

    /** Emitted by the operator-level shutdown channel to terminate the controller. */
    record EndTask() implements InputEvent {
    }
}
