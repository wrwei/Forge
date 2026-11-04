package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Raw AUV data and the obstacle-derived quantities read by the LRE.
 * For an index with no obstacle the distances are a safe large distance and
 * the obstacle field accessors are zero.
 */
public final class Sensor {

    /** Distance returned when there is no obstacle at an index (metres). */
    @RoboChartType("real") private static final double NO_OBSTACLE_DIST = 1000000.0;

    /** Index returned when no obstacle of the requested kind exists; never stored in the register. */
    @RoboChartType("nat") private static final int NO_OBSTACLE_INDEX = 2147483647;

    private final ObstacleRegister register;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records the latest raw AUV readings. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth below the surface (metres). */
    @RoboChartType("real") public double depth() { return depth; }

    /** AUV north-south velocity (m/s). */
    @RoboChartType("real") public double nsVel() { return nsVel; }

    /** AUV east-west velocity (m/s). */
    @RoboChartType("real") public double ewVel() { return ewVel; }

    /** AUV rate of climb (m/s). */
    @RoboChartType("real") public double rateOfClimb() { return rateOfClimb; }

    /** Horizontal distance to the obstacle at the index: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (metres). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at the index: |depth - obs_depth| (metres). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall distance to the obstacle at the index: sqrt(hdist^2 + vdist^2) (metres). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at the index (metres). */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at the index (metres). */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at the index (m/s). */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at the index (m/s). */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the smallest odist, or the no-obstacle index. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int closest = NO_OBSTACLE_INDEX;
        double closestDist = NO_OBSTACLE_DIST;
        boolean found = false;
        for (Integer index : register.staticIndices()) {
            double distance = odist(index);
            if (!found || distance < closestDist) {
                closest = index;
                closestDist = distance;
                found = true;
            }
        }
        return closest;
    }

    /** Index of the dynamic obstacle with the smallest odist, or the no-obstacle index. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int closest = NO_OBSTACLE_INDEX;
        double closestDist = NO_OBSTACLE_DIST;
        boolean found = false;
        for (Integer index : register.dynamicIndices()) {
            double distance = odist(index);
            if (!found || distance < closestDist) {
                closest = index;
                closestDist = distance;
                found = true;
            }
        }
        return closest;
    }
}
