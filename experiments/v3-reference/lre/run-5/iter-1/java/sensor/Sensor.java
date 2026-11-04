package lre.sensor;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;

/**
 * Environmental data of the AUV and its obstacle register (LRE-DM5).
 * For an index with no obstacle (including {@link LreConstants#NO_OBSTACLE}),
 * the distance functions return {@link LreConstants#SAFE_LARGE_DIST} and the
 * field accessors return zero.
 */
public final class Sensor {

    private final ObstacleRegister register;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Creates a sensor that reads obstacles from {@code register}; raw readings start at zero. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records the latest raw readings of the AUV. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
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

    /** Horizontal distance to obstacle {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to obstacle {@code index}: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        return Math.abs(depth - register.lookup(index).obs_depth());
    }

    /** Overall Euclidean distance to obstacle {@code index}: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** ns_rel_dist of obstacle {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** ew_rel_dist of obstacle {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** obs_ns_vel of obstacle {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** obs_ew_vel of obstacle {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the least odist (the lowest index on a
     * tie), or {@link LreConstants#NO_OBSTACLE} if there is none.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int closest = LreConstants.NO_OBSTACLE;
        double closestDist = 0.0;
        for (Integer index : register.staticIndices()) {
            double dist = odist(index);
            if (closest == LreConstants.NO_OBSTACLE || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }

    /**
     * Index of the dynamic obstacle with the least odist (the lowest index on a
     * tie), or {@link LreConstants#NO_OBSTACLE} if there is none.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int closest = LreConstants.NO_OBSTACLE;
        double closestDist = 0.0;
        for (Integer index : register.dynamicIndices()) {
            double dist = odist(index);
            if (closest == LreConstants.NO_OBSTACLE || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }
}
