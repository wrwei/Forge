package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's sensor: its raw environmental data, the obstacle register, and the
 * distance, obstacle-field and closest-obstacle functions derived from them.
 * For an index with no obstacle, the distance functions return a safe large
 * distance and the obstacle-field accessors return zero.
 */
public final class Sensor {

    /** Distance reported for an index with no obstacle; far beyond every threshold. */
    @RoboChartType("real") private static final double NO_OBSTACLE_DISTANCE = 1000000.0;

    private final ObstacleRegister obstacles = new ObstacleRegister();
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Records a new reading of the AUV's depth (m) and velocities (m/s). */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The register of obstacles this sensor reports on. */
    public ObstacleRegister obstacles() {
        return obstacles;
    }

    /** The AUV's depth below the surface, in metres. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** The AUV's north-south velocity, in m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** The AUV's east-west velocity, in m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** The AUV's rate of climb (vertical velocity), in m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = obstacles.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}: |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.abs(depth - obstacles.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south distance from the AUV to the obstacle at {@code index}, or 0 if none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).ns_rel_dist();
    }

    /** East-west distance from the AUV to the obstacle at {@code index}, or 0 if none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, or 0 if none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, or 0 if none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest overall distance, or
     * {@link ObstacleRegister#NO_OBSTACLE} if there is none.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest overall distance, or
     * {@link ObstacleRegister#NO_OBSTACLE} if there is none.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // Ties go to the lower index, since candidates arrive in increasing order.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = 0.0;
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
