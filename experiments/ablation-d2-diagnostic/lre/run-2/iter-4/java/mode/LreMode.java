package lre.mode;

/** The four operating modes of the Last Response Engine (LRE-DM1). */
public enum LreMode {
    /** Operator Control Mode — LRE inactive, operator inputs passed through. */
    OCM,
    /** Main Operating Mode — autonomous control at normal speed. */
    MOM,
    /** High Caution Mode — near an obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode — potential collision detected. */
    CAM
}
