package chemdetector.movement;

/**
 * Modes of the movement state machine
 * (CD-MV-FR1..7, plus the {@link #Final} sink that absorbs the {@code j1}
 * final state).
 */
public enum MovementMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found,
    Final
}
