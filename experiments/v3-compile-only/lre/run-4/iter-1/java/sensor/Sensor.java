package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data (LRE-DM5): its own depth and velocities, and
 * the obstacle register with the distance, field-accessor and selection
 * functions over it. Distances in metres, velocities in m/s.
 *
 * <p>An index with no obstacle (in particular {@link #NO_OBSTACLE}) has
 * distance {@code SAFE_DISTANCE} and zero relative distances and velocities.
 */
public final class Sensor {

    /**
     * Index returned by the selection functions when no obstacle of the kind
     * exists. The requirements give -1; the model's integers are natural
     * numbers, so 0 is used and the register never holds identifier 0.
     */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    /** Distance reported for an index with no obstacle: far beyond every threshold. */
    @RoboChartType("real") private static final double SAFE_DISTANCE = 1000000.0;

    private final ObstacleRegister obstacleRegister = new ObstacleRegister();

    @RoboChartType("real") private double depth = 0.0;
    @RoboChartType("real") private double nsVel = 0.0;
    @RoboChartType("real") private double ewVel = 0.0;
    @RoboChartType("real") private double rateOfClimb = 0.0;

    /** Records a new reading of the AUV's own state. */
    public void updateAuvState(@RoboChartType("real") double newDepth,
                               @RoboChartType("real") double newNsVel,
                               @RoboChartType("real") double newEwVel,
                               @RoboChartType("real") double newRateOfClimb) {
        this.depth = newDepth;
        this.nsVel = newNsVel;
        this.ewVel = newEwVel;
        this.rateOfClimb = newRateOfClimb;
    }

    /** The register of known obstacles, which the environment keeps up to date. */
    public ObstacleRegister obstacleRegister() {
        return obstacleRegister;
    }

    /** AUV depth below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity (positive when climbing). */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obstacle = obstacleRegister.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obstacle = obstacleRegister.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall distance to the obstacle: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return SAFE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** The obstacle's north-south relative distance, or 0 if there is none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return 0.0;
        }
        return obstacleRegister.lookup(index).ns_rel_dist();
    }

    /** The obstacle's east-west relative distance, or 0 if there is none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return 0.0;
        }
        return obstacleRegister.lookup(index).ew_rel_dist();
    }

    /** The obstacle's north-south velocity, or 0 if there is none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return 0.0;
        }
        return obstacleRegister.lookup(index).obs_ns_vel();
    }

    /** The obstacle's east-west velocity, or 0 if there is none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return 0.0;
        }
        return obstacleRegister.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest odist (the lowest index
     * on a tie), or {@link #NO_OBSTACLE} if there is no static obstacle.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacleRegister.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest odist (the lowest index
     * on a tie), or {@link #NO_OBSTACLE} if there is no dynamic obstacle.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacleRegister.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int best = NO_OBSTACLE;
        double bestDistance = 0.0;
        for (Integer candidate : candidates) {
            double distance = odist(candidate);
            if (best == NO_OBSTACLE || distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }
}
