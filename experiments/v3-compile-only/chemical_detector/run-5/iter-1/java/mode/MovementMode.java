package chemical_detector.mode;

/** States of the movement subsystem; the initial state first. */
public enum MovementMode {
    /** Random-walk search (CD-MV-FR1). */
    WAITING,
    /** Moving in the commanded direction (CD-MV-FR2). */
    GOING,
    /** The source was found; the robot is halted (CD-MV-FR3). */
    FOUND,
    /** Steering away from an obstacle (CD-MV-FR4). */
    AVOIDING,
    /** Resuming motion after an avoidance manoeuvre (CD-MV-FR5). */
    TRYING_AGAIN,
    /** A second obstacle in one evasion sequence (CD-MV-FR6). */
    AVOIDING_AGAIN,
    /** Recovery after stuck detection (CD-MV-FR7). */
    GETTING_OUT
}
