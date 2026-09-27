package chemical_detector.mode;

/**
 * States of the movement controller. {@code Waiting} is initial.
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
