package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1); OCM, the start-up mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE passes operator inputs to the autopilot. */
    OCM,
    /** Main Operating Mode: autonomous behaviour at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a dynamic obstacle is on a collision course. */
    CAM
}
