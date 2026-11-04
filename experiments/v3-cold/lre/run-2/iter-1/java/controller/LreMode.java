package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1); OCM, the start-up mode,
 * comes first.
 */
public enum LreMode {
    /** Operator Control Mode: operator requests pass straight through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: dynamic obstacle on a collision course. */
    CAM
}
