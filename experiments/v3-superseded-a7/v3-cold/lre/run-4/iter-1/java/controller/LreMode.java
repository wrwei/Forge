package lre.controller;

/** The four operating modes of the LRE (LRE-DM1); OCM, the start-up mode, is first. */
public enum LreMode {
    /** Operator Control Mode: the LRE is inactive and passes operator requests through. */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed. */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity. */
    HCM,
    /** Collision Avoidance Mode: a collision with a dynamic obstacle is possible. */
    CAM
}
