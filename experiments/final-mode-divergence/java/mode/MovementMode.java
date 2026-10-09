package chemdetector.mode;

/**
 * Operating modes of the movement subsystem state machine
 * (CD-MV-FR1..7). {@code Final} is the terminal (final) state j1.
 */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Final
}
