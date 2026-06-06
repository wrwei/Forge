package lre.datamodel;

/**
 * An obstacle observed by the AUV (LRE-DM2). An obstacle is static if
 * both velocity components are zero; otherwise it is dynamic.
 *
 * @param nsRelDist relative distance north-south, metres
 * @param ewRelDist relative distance east-west, metres
 * @param obsDepth  obstacle depth, metres
 * @param obsNsVel  obstacle velocity north-south, m/s
 * @param obsEwVel  obstacle velocity east-west, m/s
 * @param obsRoc    obstacle rate of climb, m/s
 */
public record Obstacle(
        double nsRelDist,
        double ewRelDist,
        double obsDepth,
        double obsNsVel,
        double obsEwVel,
        double obsRoc) {

    /** True when both horizontal velocity components are zero (LRE-DM2). */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }
}
