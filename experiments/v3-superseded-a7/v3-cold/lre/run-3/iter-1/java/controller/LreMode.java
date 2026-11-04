package lre.controller;

/**
 * The LRE's four operating modes (LRE-DM1); OCM, the start-up mode, is first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE passes operator inputs through. */
    OCM,
    /** Main Operating Mode: autonomous behaviour at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision detected. */
    CAM
}
