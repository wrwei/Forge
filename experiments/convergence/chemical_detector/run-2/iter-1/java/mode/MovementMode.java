package chemdetector.mode;

/**
 * Operating modes of the movement subsystem (CD-MV-FR1..7). Found is the
 * terminal live mode (the specification's Found -> j1 final transition is
 * replaced by remaining in Found, so the extracted state machine has no
 * Final state).
 */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
