package lre.mode;

/**
 * The four operating modes of the Last Response Engine (LRE-DM1).
 *
 * <ul>
 *   <li>OCM: Operator Control Mode -- LRE is inactive, passes operator inputs through.</li>
 *   <li>MOM: Main Operating Mode -- autonomous control at normal speed.</li>
 *   <li>HCM: High Caution Mode -- AUV near static obstacle, reduced velocity.</li>
 *   <li>CAM: Collision Avoidance Mode -- evasive manoeuvre.</li>
 * </ul>
 */
public enum LreMode {
    OCM,
    MOM,
    HCM,
    CAM
}
