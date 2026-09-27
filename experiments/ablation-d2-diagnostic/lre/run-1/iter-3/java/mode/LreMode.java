package lre.mode;

/** The four operating modes of the Last Response Engine (LRE-DM1). */
public enum LreMode {
    /** Operator Control Mode — initial mode, LRE inactive (LRE-FR1). */
    OCM,
    /** Main Operating Mode — autonomous control at normal speed (LRE-FR2). */
    MOM,
    /** High Caution Mode — reduced velocity near an obstacle (LRE-FR3). */
    HCM,
    /** Collision Avoidance Mode — evasive manoeuvre (LRE-FR4). */
    CAM
}
