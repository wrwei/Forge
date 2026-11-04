package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the quantities derived from its obstacle
 * register (LRE-DM5, LRE-SF1..SF3).
 *
 * <p>For an index with no obstacle, the distance functions return a safe large
 * distance and the field accessors return zero. The selection functions return
 * an index the register never holds when there is no matching obstacle.
 */
public final class Sensor {

    /** Distance reported for an absent obstacle, in metres; far beyond every threshold. */
    @RoboChartType("real") private static final double SAFE_LARGE_DISTANCE = 1000000.0;

    private final ObstacleRegister register;

    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Creates a sensor reading obstacles from the given register. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records the latest raw readings of the AUV. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth, in metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity (rate of climb), in m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle, in metres; zero if absent. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle, in metres; zero if absent. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle, in m/s; zero if absent. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle, in m/s; zero if absent. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest odist (lowest index on a tie),
     * or an index the register never holds if there is no static obstacle.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest odist (lowest index on a tie),
     * or an index the register never holds if there is no dynamic obstacle.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    @RoboChartType("nat")
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = SAFE_LARGE_DISTANCE;
        boolean found = false;
        for (Integer index : candidates) {
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
