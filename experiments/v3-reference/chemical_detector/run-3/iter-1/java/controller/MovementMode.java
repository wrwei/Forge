package chemical_detector.controller;

/** States of the movement subsystem; the initial state first. */
public enum MovementMode {
    /** Random-walk search between readings (CD-MV-FR1). */
    WAITING,
    /** Moving at lv in the commanded direction (CD-MV-FR2). */
    GOING,
    /** The source is found; the robot is halted (CD-MV-FR3). */
    FOUND,
    /** Steering away from an obstacle (CD-MV-FR4). */
    AVOIDING,
    /** Resuming motion after an avoidance manoeuvre (CD-MV-FR5). */
    TRYING_AGAIN,
    /** A second obstacle in one evasion sequence (CD-MV-FR6). */
    AVOIDING_AGAIN,
    /** Recovering from being stuck (CD-MV-FR7). */
    GETTING_OUT
}
