package chemical_detector.controller;

/**
 * States of the movement subsystem; the initial state first.
 */
public enum MovementMode {
    /** Random-walk search (CD-MV-FR1). */
    WAITING,
    /** Moving at lv in direction a (CD-MV-FR2). */
    GOING,
    /** Source confirmed; halted (CD-MV-FR3). */
    FOUND,
    /** Steering away from an obstacle (CD-MV-FR4). */
    AVOIDING,
    /** Resuming motion after an avoidance manoeuvre (CD-MV-FR5). */
    TRYING_AGAIN,
    /** Second obstacle in one evasion sequence; stuck check (CD-MV-FR6). */
    AVOIDING_AGAIN,
    /** Recovery manoeuvre after stuck detection (CD-MV-FR7). */
    GETTING_OUT
}
