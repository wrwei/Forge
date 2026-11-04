package lre.controller;

/** The LRE's four operating modes; OCM, the start-up mode, comes first. */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator requests through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near an obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision detected, evasive manoeuvre. */
    CAM
}
