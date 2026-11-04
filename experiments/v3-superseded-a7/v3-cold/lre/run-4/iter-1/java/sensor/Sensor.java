package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's sensor interface (LRE-DM5): the raw readings of depth and velocity,
 * and the functions over the obstacle register that the LRE reads each step.
 * An index with no registered obstacle (in particular
 * {@link ObstacleRegister#NO_OBSTACLE}) has a safe large distance and zero
 * relative position and velocity.
 */
public final class Sensor {

    // Distance reported for an index with no obstacle, metres. The requirements
    // ask for "a safe large distance" without giving one.
    private static final double NO_OBSTACLE_DISTANCE = 100000.0;

    private final ObstacleRegister obstacles;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** A sensor reading the given obstacle register. */
    public Sensor(ObstacleRegister obstacles) {
        this.obstacles = obstacles;
    }

    /** Records the latest raw readings of depth and velocity. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity, m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity, m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV rate of climb (vertical velocity), m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle with the given index, sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle with the given index, |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle with the given index, sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle with the given index, metres; zero if none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle with the given index, metres; zero if none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle with the given index, m/s; zero if none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle with the given index, m/s; zero if none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the least overall distance, or
     * {@link ObstacleRegister#NO_OBSTACLE} if there is none (LRE-OP3).
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the least overall distance, or
     * {@link ObstacleRegister#NO_OBSTACLE} if there is none (LRE-OP4).
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // The candidate with the least odist; on a tie, the lowest identifier.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = 0.0;
        boolean found = false;
        for (Integer index : candidates) {
            double distance = odist(index);
            if (!found || distance < closestDistance) {
                closest = index;
                closestDistance = distance;
                found = true;
            }
        }
        return closest;
    }
}
