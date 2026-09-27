package chemical_detector.mode;

/**
 * Operating modes of the movement subsystem. {@code Waiting} is initial.
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
