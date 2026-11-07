package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's sensor service (LRE-DM5): raw environmental data, the obstacle
 * register, and the distance and selection functions over it (LRE-SF1..3).
 * For an identifier with no obstacle, the distance functions return a safe
 * large distance and the field accessors return zero.
 */
public final class Sensor {

    /** Distance reported for an identifier with no obstacle (m). */
    private static final double NO_OBSTACLE_DIST = 1000000.0;

    private final ObstacleRegister register = new ObstacleRegister();

    @RoboChartType("real")
    private double auvDepth;

    @RoboChartType("real")
    private double auvNsVel;

    @RoboChartType("real")
    private double auvEwVel;

    @RoboChartType("real")
    private double auvRateOfClimb;

    /**
     * Records a new set of raw readings.
     *
     * @param depthReading       depth below the surface (m)
     * @param nsVelReading       north-south horizontal velocity (m/s)
     * @param ewVelReading       east-west horizontal velocity (m/s)
     * @param rateOfClimbReading vertical velocity (m/s)
     */
    public void update(@RoboChartType("real") double depthReading,
                       @RoboChartType("real") double nsVelReading,
                       @RoboChartType("real") double ewVelReading,
                       @RoboChartType("real") double rateOfClimbReading) {
        this.auvDepth = depthReading;
        this.auvNsVel = nsVelReading;
        this.auvEwVel = ewVelReading;
        this.auvRateOfClimb = rateOfClimbReading;
    }

    /**
     * The register of obstacles currently known to the AUV.
     *
     * @return the obstacle register
     */
    public ObstacleRegister obstacleRegister() {
        return register;
    }

    /** @return AUV depth below the surface (m) */
    @RoboChartType("real")
    public double depth() {
        return auvDepth;
    }

    /** @return AUV north-south horizontal velocity (m/s) */
    @RoboChartType("real")
    public double nsVel() {
        return auvNsVel;
    }

    /** @return AUV east-west horizontal velocity (m/s) */
    @RoboChartType("real")
    public double ewVel() {
        return auvEwVel;
    }

    /** @return AUV vertical velocity (m/s) */
    @RoboChartType("real")
    public double rateOfClimb() {
        return auvRateOfClimb;
    }

    /**
     * Horizontal distance to an obstacle (LRE-SF1):
     * sqrt(ns_rel_dist^2 + ew_rel_dist^2).
     *
     * @param index obstacle identifier
     * @return the distance (m)
     */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /**
     * Vertical distance to an obstacle (LRE-SF2): |depth - obs_depth|.
     *
     * @param index obstacle identifier
     * @return the distance (m)
     */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.abs(auvDepth - obstacle.obs_depth());
    }

    /**
     * Overall Euclidean distance to an obstacle (LRE-SF3):
     * sqrt(hdist^2 + vdist^2).
     *
     * @param index obstacle identifier
     * @return the distance (m)
     */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /**
     * @param index obstacle identifier
     * @return the obstacle's north-south relative distance (m), or 0
     */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /**
     * @param index obstacle identifier
     * @return the obstacle's east-west relative distance (m), or 0
     */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /**
     * @param index obstacle identifier
     * @return the obstacle's north-south velocity (m/s), or 0
     */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /**
     * @param index obstacle identifier
     * @return the obstacle's east-west velocity (m/s), or 0
     */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /**
     * @return identifier of the static obstacle with the least odist, or 0
     *         if there is no static obstacle
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDist = NO_OBSTACLE_DIST;
        for (Integer index : register.staticIndices()) {
            double dist = odist(index);
            if (best == ObstacleRegister.NO_OBSTACLE || dist < bestDist) {
                best = index;
                bestDist = dist;
            }
        }
        return best;
    }

    /**
     * @return identifier of the dynamic obstacle with the least odist, or 0
     *         if there is no dynamic obstacle
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDist = NO_OBSTACLE_DIST;
        for (Integer index : register.dynamicIndices()) {
            double dist = odist(index);
            if (best == ObstacleRegister.NO_OBSTACLE || dist < bestDist) {
                best = index;
                bestDist = dist;
            }
        }
        return best;
    }
}
