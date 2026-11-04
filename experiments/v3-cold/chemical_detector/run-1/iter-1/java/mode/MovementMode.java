package chemical_detector.mode;

/**
 * Modes of the movement controller; Waiting is the initial mode.
 * Halted is the terminal mode that follows Found.
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
