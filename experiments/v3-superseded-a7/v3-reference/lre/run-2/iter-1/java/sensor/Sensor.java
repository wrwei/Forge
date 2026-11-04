package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and the quantities derived from its obstacle
 * register (LRE-DM5, LRE-SF1..SF3). An index that names no obstacle gives a
 * safe large distance from the distance functions and zero from the field
 * accessors.
 */
public final class Sensor {

    /** Distance reported for an index that names no obstacle, metres; above every threshold. */
    @RoboChartType("real") private static final double SAFE_LARGE_DISTANCE = 100000.0;

    private final ObstacleRegister obstacles;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Creates a sensor over the given obstacle register. */
    public Sensor(ObstacleRegister obstacles) {
        this.obstacles = obstacles;
    }

    /** Stores the latest raw readings of the AUV. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Depth of the AUV, metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** North-south horizontal velocity of the AUV, m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** East-west horizontal velocity of the AUV, m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** Rate of climb (vertical velocity) of the AUV, m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at {@code index} (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index} (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.abs(depth - obstacles.lookup(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index} (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at {@code index}, metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}, metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least odist, or 0 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or 0 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // The candidate with the least odist; the lowest index wins a tie.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = Double.MAX_VALUE;
        for (Integer candidate : candidates) {
            double distance = odist(candidate);
            if (distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        return closest;
    }
}
