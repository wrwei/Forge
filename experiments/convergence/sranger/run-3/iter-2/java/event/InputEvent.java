package sranger.event;

/**
 * Input events received by the SRanger controller (SR-DM3). All three are
 * signal events (no payload):
 * <ul>
 *   <li>{@link Obstacle} — IR sensor framework crossed below obstacleThreshold;</li>
 *   <li>{@link Tick} — system-framework control cycle;</li>
 *   <li>{@link EndTask} — operator-level shutdown.</li>
 * </ul>
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
