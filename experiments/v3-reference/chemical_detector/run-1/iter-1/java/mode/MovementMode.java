package chemical_detector.mode;

/**
 * States of the movement subsystem; Waiting is initial.
 * Halted is the final state reached from Found.
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
