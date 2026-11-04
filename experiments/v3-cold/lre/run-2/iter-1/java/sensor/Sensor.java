package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle register, with the
 * derived distance, accessor and selection functions (LRE-DM5).
 *
 * <p>For an index with no obstacle, the distance functions return
 * {@link #NO_OBSTACLE_DISTANCE} and the field accessors return zero.
 */
public final class Sensor {

    // The "safe large distance" for an absent obstacle: far beyond every
    // threshold in LreConstants.
    @RoboChartType("real") private static final double NO_OBSTACLE_DISTANCE = 1000.0;

    private final ObstacleRegister register = new ObstacleRegister();
    @RoboChartType("real") private double depth = 0.0;
    @RoboChartType("real") private double ns_vel = 0.0;
    @RoboChartType("real") private double ew_vel = 0.0;
    @RoboChartType("real") private double rate_of_climb = 0.0;

    /** Records the latest AUV readings. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double ns_vel,
            @RoboChartType("real") double ew_vel, @RoboChartType("real") double rate_of_climb) {
        this.depth = depth;
        this.ns_vel = ns_vel;
        this.ew_vel = ew_vel;
        this.rate_of_climb = rate_of_climb;
    }

    /** The register of obstacles currently known to the AUV. */
    public ObstacleRegister obstacleRegister() {
        return register;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real") public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity, m/s. */
    @RoboChartType("real") public double ns_vel() {
        return ns_vel;
    }

    /** AUV east-west horizontal velocity, m/s. */
    @RoboChartType("real") public double ew_vel() {
        return ew_vel;
    }

    /** AUV rate of climb (vertical velocity), m/s. */
    @RoboChartType("real") public double rate_of_climb() {
        return rate_of_climb;
    }

    /** Horizontal distance to the obstacle at {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (LRE-SF1). */
    @RoboChartType("real") public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}: |depth - obs_depth| (LRE-SF2). */
    @RoboChartType("real") public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.abs(depth - register.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}: sqrt(hdist^2 + vdist^2) (LRE-SF3). */
    @RoboChartType("real") public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at {@code index}. */
    @RoboChartType("real") public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}. */
    @RoboChartType("real") public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}. */
    @RoboChartType("real") public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}. */
    @RoboChartType("real") public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with minimum odist, or {@link ObstacleRegister#NO_OBSTACLE} if none. */
    @RoboChartType("nat") public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with minimum odist, or {@link ObstacleRegister#NO_OBSTACLE} if none. */
    @RoboChartType("nat") public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    // Ties go to the lowest identifier.
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
