package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's environmental data and the obstacle functions derived from it (LRE-DM5,
 * LRE-SF1..3). An index that names no obstacle yields a safe large distance from the
 * distance functions and zero from the field accessors.
 */
public final class Sensor {

    /** Index returned by the selection functions when no obstacle of the kind exists. */
    @RoboChartType("nat")
    public static final int NO_OBSTACLE = 0;

    @RoboChartType("real")
    private static final double SAFE_LARGE_DISTANCE = 1000000.0;

    private final ObstacleRegister register;
    @RoboChartType("real") private double depthReading;
    @RoboChartType("real") private double nsVelReading;
    @RoboChartType("real") private double ewVelReading;
    @RoboChartType("real") private double rocReading;

    /** Creates a sensor over the given obstacle register. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records the latest AUV readings (metres below surface, m/s). */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depthReading = depth;
        this.nsVelReading = nsVel;
        this.ewVelReading = ewVel;
        this.rocReading = rateOfClimb;
    }

    /** AUV depth, in metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depthReading;
    }

    /** AUV north-south velocity, in m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVelReading;
    }

    /** AUV east-west velocity, in m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVelReading;
    }

    /** AUV rate of climb (vertical velocity), in m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rocReading;
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
        return Math.abs(depthReading - obstacle.obs_depth());
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

    /** North-south distance of the obstacle relative to the AUV, in metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** East-west distance of the obstacle relative to the AUV, in metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle, in m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle, in m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least odist, or NO_OBSTACLE if there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or NO_OBSTACLE if there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    // Ties go to the lowest index, since candidates arrive in ascending order.
    private int closestOf(List<Integer> candidates) {
        int best = NO_OBSTACLE;
        double bestDistance = SAFE_LARGE_DISTANCE;
        for (int index : candidates) {
            double distance = odist(index);
            if (best == NO_OBSTACLE || distance < bestDistance) {
                best = index;
                bestDistance = distance;
            }
        }
        return best;
    }
}
