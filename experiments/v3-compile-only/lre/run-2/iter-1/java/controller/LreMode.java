package lre.controller;

/**
 * The four operating modes of the Last Response Engine. OCM, the mode at
 * start-up, is listed first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator requests through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal operating speed. */
    MOM,
    /** High Caution Mode: the AUV is near a static obstacle and runs at reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a dynamic obstacle is on a collision course. */
    CAM
}
