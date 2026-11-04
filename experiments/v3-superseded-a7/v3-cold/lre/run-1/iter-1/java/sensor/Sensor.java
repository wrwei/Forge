package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;

/**
 * The AUV's environmental data and the quantities derived from it (LRE-DM5).
 * Obstacles are held in an {@link ObstacleRegister} with indices from 1;
 * {@link LreConstants#NO_OBSTACLE} (0) means "no such obstacle". For an index
 * with no obstacle the distance functions return
 * {@link LreConstants#NO_OBSTACLE_DIST} and the field accessors return zero.
 */
public final class Sensor {

    private final ObstacleRegister register = new ObstacleRegister();

    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /**
     * Records a new reading of the AUV's own state.
     *
     * @param depth       metres below the surface
     * @param nsVel       north-south horizontal velocity (m/s)
     * @param ewVel       east-west horizontal velocity (m/s)
     * @param rateOfClimb vertical velocity (m/s)
     */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Adds or replaces the obstacle at {@code index}, which must be at least 1. */
    public void putObstacle(@RoboChartType("nat") int index, Obstacle obstacle) {
        register.put(index, obstacle);
    }

    /** Removes the obstacle at {@code index}, if any. */
    public void removeObstacle(@RoboChartType("nat") int index) {
        register.remove(index);
    }

    // --- Raw AUV data ---

    /** AUV depth, metres below the surface. */
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

    // --- Distance functions (LRE-SF1, LRE-SF2, LRE-SF3) ---

    /** Horizontal distance to the obstacle at {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}: |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.NO_OBSTACLE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    // --- Obstacle field accessors ---

    /** ns_rel_dist of the obstacle at {@code index} (m), or zero if there is none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** ew_rel_dist of the obstacle at {@code index} (m), or zero if there is none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** obs_ns_vel of the obstacle at {@code index} (m/s), or zero if there is none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** obs_ew_vel of the obstacle at {@code index} (m/s), or zero if there is none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    // --- Selection functions ---

    /** Index of the static obstacle with the least odist, or NO_OBSTACLE if there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or NO_OBSTACLE if there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    /** The index in {@code indices} with the least odist; ties go to the lowest index. */
    private int closestOf(List<Integer> indices) {
        int closest = LreConstants.NO_OBSTACLE;
        double closestDist = LreConstants.NO_OBSTACLE_DIST;
        for (Integer index : indices) {
            double dist = odist(index);
            if (closest == LreConstants.NO_OBSTACLE || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }
}
