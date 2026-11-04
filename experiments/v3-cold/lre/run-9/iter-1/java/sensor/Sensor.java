package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and the quantities derived from it (LRE-DM5,
 * LRE-SF1 to LRE-SF3).
 *
 * <p>An index that names no registered obstacle (in particular
 * {@link ObstacleRegister#NO_OBSTACLE}) reads as "no obstacle": the distance
 * functions and the relative-distance accessors return a safe large distance,
 * and the velocity accessors return zero.
 */
public final class Sensor {

    /** Distance, in metres, reported for an obstacle that does not exist. */
    @RoboChartType("real") private static final double NO_OBSTACLE_DISTANCE = 1000000.0;

    private final ObstacleRegister register = new ObstacleRegister();

    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Stores the latest raw readings of the AUV's own depth and velocity. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The register of obstacles the sensor currently sees. */
    public ObstacleRegister obstacles() {
        return register;
    }

    /** Depth of the AUV, in metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** North-south horizontal velocity of the AUV, in m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** East-west horizontal velocity of the AUV, in m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** Vertical velocity (rate of climb) of the AUV, in m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south distance of the obstacle relative to the AUV, in metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** East-west distance of the obstacle relative to the AUV, in metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle, in m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle, in m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest overall distance, or
     * {@link ObstacleRegister#NO_OBSTACLE} when there is none.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest overall distance, or
     * {@link ObstacleRegister#NO_OBSTACLE} when there is none.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    /** The candidate with the smallest odist; ties go to the lowest index. */
    private int closestOf(List<Integer> candidates) {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDistance = 0.0;
        for (int i = 0; i < candidates.size(); i++) {
            int index = candidates.get(i);
            double distance = odist(index);
            if (best == ObstacleRegister.NO_OBSTACLE || distance < bestDistance) {
                best = index;
                bestDistance = distance;
            }
        }
        return best;
    }
}
