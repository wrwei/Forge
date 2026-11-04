package chemical_detector.controller;

/**
 * The states of the movement subsystem; Waiting is the initial state.
 */
public enum MovementMode {
    /** CD-MV-FR1: unbounded random-walk search. */
    Waiting,
    /** CD-MV-FR2: moving at lv in the commanded direction. */
    Going,
    /** CD-MV-FR3: the source has been found; the robot is halted. */
    Found,
    /** CD-MV-FR4: steering away from an obstacle. */
    Avoiding,
    /** CD-MV-FR5: resuming motion after an avoidance manoeuvre. */
    TryingAgain,
    /** CD-MV-FR6: a second obstacle in the same evasion sequence; deciding whether stuck. */
    AvoidingAgain,
    /** CD-MV-FR7: recovering from being stuck. */
    GettingOut
}
