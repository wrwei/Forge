package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle register, with the
 * distance, accessor and selection functions the LRE derives from them.
 * For an index with no obstacle, the distance functions return a safe large
 * distance and the field accessors return zero.
 */
public final class Sensor {

    // Far beyond every threshold in LreConstants (m).
    @RoboChartType("real") private static final double NO_OBSTACLE_DIST = 100000.0;

    private final ObstacleRegister obstacles;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Creates a sensor that reads obstacles from the given register. */
    public Sensor(ObstacleRegister obstacles) {
        this.obstacles = obstacles;
    }

    /** Records the latest AUV readings: depth (m), horizontal velocities and rate of climb (m/s). */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth below the surface (m). */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity (m/s). */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity (m/s). */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity (m/s). */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (m). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle: |depth - obs_depth| (m). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle: sqrt(hdist^2 + vdist^2) (m). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle (m), or zero if there is none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle (m), or zero if there is none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle (m/s), or zero if there is none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle (m/s), or zero if there is none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least odist, or {@link ObstacleRegister#NO_OBSTACLE}. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or {@link ObstacleRegister#NO_OBSTACLE}. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // The candidate with the least odist; on a tie, the lowest index.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDist = NO_OBSTACLE_DIST;
        for (int index : candidates) {
            double dist = odist(index);
            if (closest == ObstacleRegister.NO_OBSTACLE || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }
}
