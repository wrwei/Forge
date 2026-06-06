package lre.mode;

/**
 * The four operating modes of the Last Response Engine (LRE-DM1).
 *
 * - OCM: Operator Control Mode (initial)
 * - MOM: Main Operating Mode (autonomous)
 * - HCM: High Caution Mode (reduced speed)
 * - CAM: Collision Avoidance Mode (evasive)
 */
public enum LreMode {
    OCM,
    MOM,
    HCM,
    CAM
}
