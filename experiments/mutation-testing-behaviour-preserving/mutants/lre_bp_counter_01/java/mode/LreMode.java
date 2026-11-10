package lre.mode;

/**
 * The four operating modes of the Last Response Engine (LRE).
 *
 * <ul>
 *   <li>{@link #OCM} — Operator Control Mode. Initial mode; LRE passes operator
 *       inputs directly to the autopilot.</li>
 *   <li>{@link #MOM} — Main Operating Mode. Autonomous navigation at 1 m/s.</li>
 *   <li>{@link #HCM} — High Caution Mode. Reduced velocity when near static
 *       obstacles.</li>
 *   <li>{@link #CAM} — Collision Avoidance Mode. Emergency evasive manoeuvre
 *       when a dynamic obstacle is on a collision course.</li>
 * </ul>
 */
public enum LreMode {
    OCM,
    MOM,
    HCM,
    CAM
}
