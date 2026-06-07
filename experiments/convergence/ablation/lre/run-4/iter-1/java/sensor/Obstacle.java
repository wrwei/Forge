package lre.sensor;

/**
 * An obstacle known to the AUV (LRE-DM2). All distances are metres and all
 * velocities are m/s, relative to the AUV where named "rel".
 */
public record Obstacle(
        double nsRelDist,
        double ewRelDist,
        double obsDepth,
        double obsNsVel,
        double obsEwVel,
        double obsRoc) {

    /** An obstacle is static when both horizontal velocity components are zero. */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }

    /** An obstacle is dynamic when it is not static. */
    public boolean isDynamic() {
        return !isStatic();
    }
}
