package lre.controller;

/**
 * The LRE's operating modes (LRE-DM1); OCM, the initial mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: LRE inactive, operator inputs passed to the autopilot. */
    OCM,
    /** Main Operating Mode: autonomous behaviour at normal speed. */
    MOM,
    /** High Caution Mode: near an obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision detected. */
    CAM
}
