package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1); OCM, the start-up mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: LRE inactive, operator inputs passed to the autopilot. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision with a dynamic obstacle. */
    CAM
}
