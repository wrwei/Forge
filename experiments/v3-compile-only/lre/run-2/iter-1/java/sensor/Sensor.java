package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle register, with the
 * distance, field-accessor and selection functions derived from them.
 */
public final class Sensor {

    /** Distance returned when there is no obstacle at an index, metres. */
    @RoboChartType("real")
    private static final double SAFE_LARGE_DIST = 1000000.0;

    /** Field value returned when there is no obstacle at an index. */
    @RoboChartType("real")
    private static final double ABSENT_FIELD_VALUE = 0.0;

    private final ObstacleRegister obstacleRegister = new ObstacleRegister();

    @RoboChartType("real")
    private double currentDepth = 0.0;

    @RoboChartType("real")
    private double currentNsVel = 0.0;

    @RoboChartType("real")
    private double currentEwVel = 0.0;

    @RoboChartType("real")
    private double currentRateOfClimb = 0.0;

    /**
     * The register of obstacles known to the AUV.
     *
     * @return the obstacle register
     */
    public ObstacleRegister obstacleRegister() {
        return obstacleRegister;
    }

    /**
     * Records a new set of raw readings.
     *
     * @param depth       depth, metres below surface
     * @param nsVel       north-south horizontal velocity, m/s
     * @param ewVel       east-west horizontal velocity, m/s
     * @param rateOfClimb vertical velocity, m/s
     */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.currentDepth = depth;
        this.currentNsVel = nsVel;
        this.currentEwVel = ewVel;
        this.currentRateOfClimb = rateOfClimb;
    }

    /**
     * AUV depth.
     *
     * @return depth, metres below surface
     */
    @RoboChartType("real")
    public double depth() {
        return currentDepth;
    }

    /**
     * AUV north-south horizontal velocity.
     *
     * @return velocity, m/s
     */
    @RoboChartType("real")
    public double nsVel() {
        return currentNsVel;
    }

    /**
     * AUV east-west horizontal velocity.
     *
     * @return velocity, m/s
     */
    @RoboChartType("real")
    public double ewVel() {
        return currentEwVel;
    }

    /**
     * AUV rate of climb (vertical velocity).
     *
     * @return rate of climb, m/s
     */
    @RoboChartType("real")
    public double rateOfClimb() {
        return currentRateOfClimb;
    }

    /**
     * Horizontal distance to the obstacle at an index:
     * sqrt(ns_rel_dist^2 + ew_rel_dist^2).
     *
     * @param index the obstacle index
     * @return the distance, metres; a safe large distance when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = obstacleRegister.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /**
     * Vertical distance to the obstacle at an index: |depth - obs_depth|.
     *
     * @param index the obstacle index
     * @return the distance, metres; a safe large distance when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = obstacleRegister.lookup(index);
        return Math.abs(currentDepth - obstacle.obs_depth());
    }

    /**
     * Overall Euclidean distance to the obstacle at an index:
     * sqrt(hdist(index)^2 + vdist(index)^2).
     *
     * @param index the obstacle index
     * @return the distance, metres; a safe large distance when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /**
     * North-south relative distance of the obstacle at an index.
     *
     * @param index the obstacle index
     * @return ns_rel_dist, metres; zero when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return ABSENT_FIELD_VALUE;
        }
        return obstacleRegister.lookup(index).ns_rel_dist();
    }

    /**
     * East-west relative distance of the obstacle at an index.
     *
     * @param index the obstacle index
     * @return ew_rel_dist, metres; zero when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return ABSENT_FIELD_VALUE;
        }
        return obstacleRegister.lookup(index).ew_rel_dist();
    }

    /**
     * North-south velocity of the obstacle at an index.
     *
     * @param index the obstacle index
     * @return obs_ns_vel, m/s; zero when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return ABSENT_FIELD_VALUE;
        }
        return obstacleRegister.lookup(index).obs_ns_vel();
    }

    /**
     * East-west velocity of the obstacle at an index.
     *
     * @param index the obstacle index
     * @return obs_ew_vel, m/s; zero when there is no obstacle at the index
     */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacleRegister.contains(index)) {
            return ABSENT_FIELD_VALUE;
        }
        return obstacleRegister.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest overall distance; the
     * lowest index wins a tie.
     *
     * @return the index, or the reserved no-obstacle index (a natural number
     *         never present in the register) when there is no static obstacle
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacleRegister.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest overall distance; the
     * lowest index wins a tie.
     *
     * @return the index, or the reserved no-obstacle index (a natural number
     *         never present in the register) when there is no dynamic obstacle
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacleRegister.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = 0.0;
        boolean found = false;
        for (Integer candidate : candidates) {
            int index = candidate.intValue();
            double distance = odist(index);
            if (!found || distance < closestDistance) {
                closest = index;
                closestDistance = distance;
                found = true;
            }
        }
        return closest;
    }
}
