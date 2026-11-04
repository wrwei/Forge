package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and obstacle register (LRE-DM5).
 *
 * <p>For an index with no obstacle (including
 * {@link ObstacleRegister#NO_OBSTACLE}), the distance functions return a safe
 * large distance and the obstacle field accessors return zero.
 */
public final class Sensor {

    /** Distance reported for an absent obstacle, in metres; beyond every threshold. */
    @RoboChartType("real") private static final double NO_OBSTACLE_DISTANCE = 100000.0;

    private final ObstacleRegister obstacles = new ObstacleRegister();
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Records the latest raw readings. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The obstacle register held by this sensor. */
    public ObstacleRegister obstacles() {
        return obstacles;
    }

    /** Depth of the AUV, in metres below the surface. */
    @RoboChartType("real") public double depth() { return depth; }

    /** North-south horizontal velocity of the AUV, in m/s. */
    @RoboChartType("real") public double nsVel() { return nsVel; }

    /** East-west horizontal velocity of the AUV, in m/s. */
    @RoboChartType("real") public double ewVel() { return ewVel; }

    /** Vertical velocity (rate of climb) of the AUV, in m/s. */
    @RoboChartType("real") public double rateOfClimb() { return rateOfClimb; }

    /** Horizontal distance to the obstacle at the index: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at the index: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.abs(depth - obstacles.lookup(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at the index: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the index, in metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at the index, in metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at the index, in m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at the index, in m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest odist, or
     * {@link ObstacleRegister#NO_OBSTACLE} if there is none.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest odist, or
     * {@link ObstacleRegister#NO_OBSTACLE} if there is none.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // Ties go to the lowest index (candidates are in ascending order).
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = NO_OBSTACLE_DISTANCE;
        for (int index : candidates) {
            double distance = odist(index);
            if (closest == ObstacleRegister.NO_OBSTACLE || distance < closestDistance) {
                closest = index;
                closestDistance = distance;
            }
        }
        return closest;
    }
}
