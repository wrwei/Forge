package lre.controller;

/**
 * The LRE's operating modes (LRE-DM1); OCM, the start-up mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator requests through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision with a dynamic obstacle. */
    CAM
}
