package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1). The initial mode is OCM.
 */
public enum LreMode {
    /** Operator Control Mode — LRE inactive, operator passthrough. */
    OCM,
    /** Main Operating Mode — autonomous control at normal speed. */
    MOM,
    /** High Caution Mode — near an obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode — potential collision, evasive manoeuvre. */
    CAM
}
