package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;
import lre.data.Obstacle;
import lre.data.ObstacleRegister;

/**
 * The LRE's sensor interface (LRE-DM5): the AUV's raw environmental data, the
 * obstacle register, and the distance, accessor and selection functions over
 * it (LRE-SF1, LRE-SF2, LRE-SF3).
 *
 * <p>An index that names no obstacle (in particular
 * {@link ObstacleRegister#NO_INDEX}) gives a safe large distance from the
 * distance functions and zero from the field accessors.
 */
public final class Sensor {

    /** Distance returned when no obstacle exists at an index (m). */
    @RoboChartType("real") private static final double SAFE_LARGE_DIST = 100000.0;

    private final ObstacleRegister register;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /**
     * Creates a sensor over the given obstacle register, with all raw readings
     * at zero.
     */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /**
     * Records the AUV's latest raw readings.
     *
     * @param depth       metres below the surface
     * @param nsVel       north-south horizontal velocity, m/s
     * @param ewVel       east-west horizontal velocity, m/s
     * @param rateOfClimb vertical velocity, m/s
     */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The obstacle register this sensor reads. */
    public ObstacleRegister register() {
        return register;
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

    /** AUV rate of climb (vertical velocity), m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /**
     * Horizontal distance to the obstacle at the index (LRE-SF1):
     * sqrt(ns_rel_dist^2 + ew_rel_dist^2), or a safe large distance if none.
     */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /**
     * Vertical distance to the obstacle at the index (LRE-SF2):
     * |depth - obs_depth|, or a safe large distance if none.
     */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /**
     * Overall Euclidean distance to the obstacle at the index (LRE-SF3):
     * sqrt(hdist^2 + vdist^2), or a safe large distance if none.
     */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** ns_rel_dist of the obstacle at the index, or zero if none. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** ew_rel_dist of the obstacle at the index, or zero if none. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** obs_ns_vel of the obstacle at the index, or zero if none. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** obs_ew_vel of the obstacle at the index, or zero if none. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the least overall distance, or
     * {@link ObstacleRegister#NO_INDEX} if there is none. Ties go to the
     * lowest index.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the least overall distance, or
     * {@link ObstacleRegister#NO_INDEX} if there is none. Ties go to the
     * lowest index.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int best = ObstacleRegister.NO_INDEX;
        double bestDist = 0.0;
        for (Integer index : candidates) {
            double dist = odist(index);
            if (best == ObstacleRegister.NO_INDEX || dist < bestDist) {
                best = index;
                bestDist = dist;
            }
        }
        return best;
    }
}
