package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data (depth, velocity components, rate of climb) and the
 * obstacle register, with the distance, field-accessor and selection functions derived
 * from them. An index with no obstacle (including {@link ObstacleRegister#NO_OBSTACLE})
 * yields a safe large distance from the distance functions and zero from the accessors.
 */
public final class Sensor {

    /** Distance reported for an index with no obstacle: far beyond every threshold, metres. */
    @RoboChartType("real") private static final double SAFE_LARGE_DIST = 1000000.0;

    private final ObstacleRegister register = new ObstacleRegister();
    @RoboChartType("real") private double auvDepth = 0.0;
    @RoboChartType("real") private double auvNsVel = 0.0;
    @RoboChartType("real") private double auvEwVel = 0.0;
    @RoboChartType("real") private double auvRateOfClimb = 0.0;

    /** Stores a new reading of the AUV's depth (m), horizontal velocities and rate of climb (m/s). */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.auvDepth = depth;
        this.auvNsVel = nsVel;
        this.auvEwVel = ewVel;
        this.auvRateOfClimb = rateOfClimb;
    }

    /** The obstacle register this sensor holds. */
    public ObstacleRegister obstacles() {
        return register;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return auvDepth;
    }

    /** AUV north-south horizontal velocity, m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return auvNsVel;
    }

    /** AUV east-west horizontal velocity, m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return auvEwVel;
    }

    /** AUV vertical velocity (rate of climb), m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return auvRateOfClimb;
    }

    /** Horizontal distance to the obstacle: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle: |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.abs(auvDepth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south distance of the obstacle relative to the AUV, metres; zero if none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west distance of the obstacle relative to the AUV, metres; zero if none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle, m/s; zero if none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle, m/s; zero if none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least odist, or NO_OBSTACLE (0) if there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or NO_OBSTACLE (0) if there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    /** The candidate with the least odist; ties go to the lower index. */
    private int closestOf(List<Integer> candidates) {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDist = SAFE_LARGE_DIST;
        for (int index : candidates) {
            double dist = odist(index);
            if (best == ObstacleRegister.NO_OBSTACLE || dist < bestDist
                    || (dist == bestDist && index < best)) {
                best = index;
                bestDist = dist;
            }
        }
        return best;
    }
}
