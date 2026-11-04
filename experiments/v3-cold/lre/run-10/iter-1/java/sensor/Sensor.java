package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle register (LRE-DM5).
 * For an index with no registered obstacle the distance functions return
 * {@code NO_OBSTACLE_DISTANCE} and the field accessors return zero.
 */
public final class Sensor {

    // The "safe large distance" for a missing obstacle, m: far beyond every threshold.
    private static final double NO_OBSTACLE_DISTANCE = 1000000.0;

    private final ObstacleRegister register;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** A sensor reading obstacles from {@code register}. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records the latest AUV readings: depth (m), horizontal velocities and rate of climb (m/s). */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real") public double depth() { return depth; }

    /** AUV north-south velocity, m/s. */
    @RoboChartType("real") public double nsVel() { return nsVel; }

    /** AUV east-west velocity, m/s. */
    @RoboChartType("real") public double ewVel() { return ewVel; }

    /** AUV rate of climb, m/s. */
    @RoboChartType("real") public double rateOfClimb() { return rateOfClimb; }

    /** Horizontal distance to the obstacle at {@code index}, m (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}, m (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.abs(depth - register.lookup(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}, m (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at {@code index}, m. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}, m. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /** Index of the static obstacle with minimum odist, or {@link ObstacleRegister#NO_OBSTACLE}. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with minimum odist, or {@link ObstacleRegister#NO_OBSTACLE}. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    // The candidate with minimum odist; ties go to the lowest index.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = 0.0;
        for (int index : candidates) {
            double distance = odist(index);
            if (closest == ObstacleRegister.NO_OBSTACLE || distance < closestDistance) {
                closest = index;
                closestDistance = distance;
            }
        }
        return closest;
    }
}
