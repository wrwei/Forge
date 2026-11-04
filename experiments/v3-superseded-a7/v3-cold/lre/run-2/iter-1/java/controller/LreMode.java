package lre.controller;

/**
 * The four operating modes of the LRE; OCM is the mode at start up (LRE-DM1).
 */
public enum LreMode {
    /** Operator Control Mode: operator velocity and heading pass through to the autopilot. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a dynamic obstacle is on a collision course. */
    CAM
}
