package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle register (LRE-DM5), with
 * the distance functions of LRE-SF1..SF3. For an index with no obstacle the
 * distance functions return a safe large distance and the field accessors
 * return zero.
 */
public final class Sensor {

    /** Distance reported for an index that holds no obstacle, metres. */
    @RoboChartType("real") private static final double SAFE_LARGE_DIST = 1000000.0;

    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;
    private final ObstacleRegister register = new ObstacleRegister();

    /** Records the latest AUV readings: depth (m below surface) and velocities (m/s). */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The obstacle register the sensor reads from. */
    public ObstacleRegister obstacles() {
        return register;
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

    /** AUV vertical velocity (rate of climb), m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.abs(depth - register.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** ns_rel_dist of the obstacle at {@code index}, or zero if none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** ew_rel_dist of the obstacle at {@code index}, or zero if none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** obs_ns_vel of the obstacle at {@code index}, or zero if none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** obs_ew_vel of the obstacle at {@code index}, or zero if none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the least odist (lowest index on a tie),
     * or an index holding no obstacle if there is no static obstacle (LRE-OP3).
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int closest = ObstacleRegister.NO_INDEX;
        double closestDist = 0.0;
        for (int index : register.staticIndices()) {
            double dist = odist(index);
            if (closest == ObstacleRegister.NO_INDEX || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }

    /**
     * Index of the dynamic obstacle with the least odist (lowest index on a tie),
     * or an index holding no obstacle if there is no dynamic obstacle (LRE-OP4).
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int closest = ObstacleRegister.NO_INDEX;
        double closestDist = 0.0;
        for (int index : register.dynamicIndices()) {
            double dist = odist(index);
            if (closest == ObstacleRegister.NO_INDEX || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }
}
