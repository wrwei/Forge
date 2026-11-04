package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1); OCM, the start-up mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator inputs to the autopilot. */
    OCM,
    /** Main Operating Mode: the LRE controls autonomous behaviour at normal speed. */
    MOM,
    /** High Caution Mode: the AUV is near an obstacle and moves at reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a potential collision has been detected. */
    CAM
}
