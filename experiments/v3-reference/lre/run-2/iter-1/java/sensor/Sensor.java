package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and the obstacle functions derived from it
 * (LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3). Indices that name no obstacle in the
 * register (including {@link ObstacleRegister#NO_OBSTACLE}) give a safe large
 * distance from the distance functions and zero from the field accessors.
 */
public final class Sensor {

    @RoboChartType("real") private static final double SAFE_LARGE_DISTANCE = 1000000.0;

    private final ObstacleRegister register;
    @RoboChartType("real") private double depthReading;
    @RoboChartType("real") private double nsVelReading;
    @RoboChartType("real") private double ewVelReading;
    @RoboChartType("real") private double rateOfClimbReading;

    /** A sensor over {@code register}; readings are zero until the first update. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Stores the latest raw readings of the AUV. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depthReading = depth;
        this.nsVelReading = nsVel;
        this.ewVelReading = ewVel;
        this.rateOfClimbReading = rateOfClimb;
    }

    /** Depth below the surface, metres. */
    @RoboChartType("real")
    public double depth() {
        return depthReading;
    }

    /** North-south horizontal velocity, m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVelReading;
    }

    /** East-west horizontal velocity, m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVelReading;
    }

    /** Vertical velocity (rate of climb), m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimbReading;
    }

    /** Horizontal distance to the obstacle at {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}: |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.abs(depthReading - register.lookup(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** ns_rel_dist of the obstacle at {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** ew_rel_dist of the obstacle at {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** obs_ns_vel of the obstacle at {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** obs_ew_vel of the obstacle at {@code index}, or zero if there is none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the least odist (lowest index on a tie),
     * or {@link ObstacleRegister#NO_OBSTACLE} if there is no static obstacle.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the least odist (lowest index on a tie),
     * or {@link ObstacleRegister#NO_OBSTACLE} if there is no dynamic obstacle.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDistance = 0.0;
        for (Integer index : candidates) {
            double distance = odist(index);
            if (best == ObstacleRegister.NO_OBSTACLE || distance < bestDistance) {
                best = index;
                bestDistance = distance;
            }
        }
        return best;
    }
}
