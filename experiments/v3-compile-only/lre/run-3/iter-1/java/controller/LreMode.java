package lre.controller;

/**
 * The LRE's operating modes (LRE-DM1); OCM, the initial mode, comes first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE passes operator inputs through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near an obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision, evasive manoeuvre. */
    CAM
}
