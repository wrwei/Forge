package chemdetector.mode;

/**
 * Operating modes of the movement subsystem.
 *
 * <p>Found is terminal in the specification (transitions to final state
 * j1); here it stays live and absorbs further stop signals so the state
 * machine has no final state.
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
