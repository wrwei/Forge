package lre.controller;

/** The four operating modes of the LRE; OCM, the start-up mode, comes first. */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator requests through. */
    OCM,
    /** Main Operating Mode: autonomous behaviour at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: potential collision with a dynamic obstacle. */
    CAM
}
