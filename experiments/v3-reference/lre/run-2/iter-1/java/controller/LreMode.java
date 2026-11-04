package lre.controller;

/**
 * The four operating modes of the LRE (LRE-DM1); OCM, the initial mode, first.
 */
public enum LreMode {
    /** Operator Control Mode: the LRE passes operator requests through (LRE-FR1). */
    OCM,
    /** Main Operating Mode: autonomous control at normal speed (LRE-FR2). */
    MOM,
    /** High Caution Mode: near a static obstacle, reduced velocity (LRE-FR3). */
    HCM,
    /** Collision Avoidance Mode: potential collision detected (LRE-FR4). */
    CAM
}
