package chemical_detector.mode;

/**
 * Modes of the movement subsystem; the initial mode first.
 */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Halted
}
