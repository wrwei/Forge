package lre.controller;

/**
 * The four LRE operating modes (LRE-DM1); OCM, the start-up mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: LRE inactive, operator inputs passed through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near an obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision, evasive manoeuvre. */
    CAM
}
