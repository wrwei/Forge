package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and its obstacle register (LRE-DM5).
 * For an index with no obstacle, the distance functions return a safe large distance and the
 * field accessors return zero; the selection functions return index 0 when there is no candidate.
 */
public final class Sensor {

    /** Distance (m) reported for an index with no obstacle: far beyond every threshold. */
    @RoboChartType("real") private static final double NO_OBSTACLE_DIST = 1000000.0;

    private final ObstacleRegister register;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** A sensor reading obstacles from the given register. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records a new reading of the AUV's depth and velocities. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Depth of the AUV below the surface, in metres. */
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

    /** Horizontal distance to the obstacle at the index: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at the index: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double difference = depth - register.get(index).obs_depth();
        if (difference < 0.0) {
            return -difference;
        }
        return difference;
    }

    /** Overall Euclidean distance to the obstacle at the index: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at the index, in metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at the index, in metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at the index, in m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at the index, in m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least odist, or 0 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or 0 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    // The candidate with the least odist; on a tie, the lowest index.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDist = 0.0;
        boolean found = false;
        for (int index : candidates) {
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
