package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1); OCM is the initial mode.
 */
public enum LreMode {
    /** Operator Control Mode: operator inputs pass through to the autopilot. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a dynamic obstacle is on a collision course. */
    CAM
}
