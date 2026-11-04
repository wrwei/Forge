package lre.controller;

/**
 * The LRE's operating modes; OCM, the mode at start-up, comes first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator inputs through. */
    OCM,
    /** Main Operating Mode: autonomous behaviour at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, at reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a potential collision has been detected. */
    CAM
}
