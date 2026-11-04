package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's sensor (LRE-DM5): raw environmental data, the obstacle register
 * and the quantities derived from them. An index with no registered obstacle
 * (in particular 0, which means "no obstacle") has a safe large distance and
 * zero relative distances and velocities.
 */
public final class Sensor {

    // "A safe large distance" (LRE-DM5); the value is not given by the requirements.
    @RoboChartType("real") private static final double SAFE_LARGE_DISTANCE = 1000000.0;

    @RoboChartType("real") private static final double NO_OBSTACLE_FIELD = 0.0;

    private final ObstacleRegister register = new ObstacleRegister();

    @RoboChartType("real") private double depth = 0.0;
    @RoboChartType("real") private double nsVel = 0.0;
    @RoboChartType("real") private double ewVel = 0.0;
    @RoboChartType("real") private double rateOfClimb = 0.0;

    /** Records the latest raw readings of the AUV. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Registers or replaces the obstacle at {@code index} (indices start at 1). */
    public void putObstacle(@RoboChartType("nat") int index, Obstacle obstacle) {
        register.put(index, obstacle);
    }

    /** Removes the obstacle at {@code index}, if any. */
    public void removeObstacle(@RoboChartType("nat") int index) {
        register.remove(index);
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

    /** Horizontal distance to the obstacle at {@code index}, metres (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}, metres (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.abs(depth - register.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}, metres (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    /** North-south relative distance of the obstacle at {@code index}, metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_FIELD;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}, metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_FIELD;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_FIELD;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_FIELD;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the smallest odist, or 0 if there is none (LRE-OP3). */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with the smallest odist, or 0 if there is none (LRE-OP4). */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    // Ties go to the lowest index.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDist = SAFE_LARGE_DISTANCE;
        for (int index : candidates) {
            double dist = odist(index);
            if (closest == ObstacleRegister.NO_OBSTACLE || dist < closestDist) {
                closest = index;
                closestDist = dist;
            }
        }
        return closest;
    }
}
