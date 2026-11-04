package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's sensor: raw environmental readings, the obstacle register, and the distance,
 * field-accessor and selection functions derived from them. A function asked about an index
 * with no registered obstacle returns a safe large distance (distance functions) or zero
 * (field accessors); a selection function with no candidate returns index 0 ("no obstacle").
 */
public final class Sensor {

    /** Distance reported for an obstacle that does not exist (m). */
    @RoboChartType("real") private static final double SAFE_DISTANCE = 100000.0;

    private final ObstacleRegister register = new ObstacleRegister();
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Records the latest raw readings of the AUV. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The obstacle register this sensor holds. */
    public ObstacleRegister obstacles() {
        return register;
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

    /** Horizontal distance to the obstacle: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle: |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.abs(depth - register.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** The obstacle's north-south relative distance (m), or zero if it does not exist. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** The obstacle's east-west relative distance (m), or zero if it does not exist. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** The obstacle's north-south velocity (m/s), or zero if it does not exist. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** The obstacle's east-west velocity (m/s), or zero if it does not exist. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the smallest odist, or 0 if there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closest(register.staticIndices());
    }

    /** Index of the dynamic obstacle with the smallest odist, or 0 if there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closest(register.dynamicIndices());
    }

    @RoboChartType("nat")
    private int closest(List<Integer> candidates) {
        int closestIndex = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = 0.0;
        for (int index : candidates) {
            double distance = odist(index);
            if (closestIndex == ObstacleRegister.NO_OBSTACLE || distance < closestDistance) {
                closestIndex = index;
                closestDistance = distance;
            }
        }
        return closestIndex;
    }
}
