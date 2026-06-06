package chemdetector.mode;

/**
 * Operating modes of the movement subsystem. Found is the live terminal
 * mode entered when the chemical source has been confirmed (the
 * specification's final state, kept live for model extraction).
 */
public enum MovementMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found
}
