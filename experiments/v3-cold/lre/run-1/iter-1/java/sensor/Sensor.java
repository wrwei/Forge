package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and the obstacle register (LRE-DM5). When no obstacle
 * exists at an index, the distance functions return a safe large distance and the
 * field accessors return zero.
 */
public final class Sensor {

    // The safe large distance reported for an index with no obstacle, metres.
    @RoboChartType("real") private static final double NO_OBSTACLE_DIST = 100000.0;

    private final ObstacleRegister register = new ObstacleRegister();
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** Records the latest readings of the AUV's own state. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The obstacle register this sensor reads from. */
    public ObstacleRegister obstacles() {
        return register;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south velocity, m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west velocity, m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV rate of climb, m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at {@code index} (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index} (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        return Math.abs(depth - register.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index} (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest overall distance (lowest index on a tie).
     * With no static obstacle, an index that is never registered, at which the distance
     * functions report a safe large distance.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest overall distance (lowest index on a tie).
     * With no dynamic obstacle, an index that is never registered, at which the distance
     * functions report a safe large distance and the field accessors zero.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_INDEX;
        double closestDist = NO_OBSTACLE_DIST;
        for (int index : candidates) {
            double dist = odist(index);
            if (closest == ObstacleRegister.NO_INDEX || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }
}
