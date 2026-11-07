package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;
import lre.domain.Obstacle;
import lre.domain.ObstacleRegister;

/**
 * The AUV sensor (LRE-DM5): raw environmental readings, the obstacle register,
 * and the distance, field-accessor and selection functions over it
 * (LRE-SF1..SF3). For an index with no obstacle, the distance functions return
 * a safe large distance and the field accessors return zero.
 */
public final class Sensor {

    /** Distance reported for an index with no obstacle (m); far beyond every threshold. */
    @RoboChartType("real") private static final double SAFE_DISTANCE = 1000.0;

    private final ObstacleRegister register;

    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** A sensor over the given obstacle register; all readings start at zero. */
    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    /** Records a new set of raw readings from the AUV. */
    public void update(@RoboChartType("real") double newDepth,
                       @RoboChartType("real") double newNsVel,
                       @RoboChartType("real") double newEwVel,
                       @RoboChartType("real") double newRateOfClimb) {
        this.depth = newDepth;
        this.nsVel = newNsVel;
        this.ewVel = newEwVel;
        this.rateOfClimb = newRateOfClimb;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity (m/s). */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity (m/s). */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV rate of climb, its vertical velocity (m/s). */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** LRE-SF1: horizontal distance to the obstacle at {@code index}. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** LRE-SF2: vertical distance to the obstacle at {@code index}. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obstacle = register.lookup(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** LRE-SF3: overall Euclidean distance to the obstacle at {@code index}. */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at {@code index}, or zero. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}, or zero. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, or zero. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, or zero. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest overall distance (ties go
     * to the smaller index), or {@link ObstacleRegister#NO_OBSTACLE} if none.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(register.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest overall distance (ties go
     * to the smaller index), or {@link ObstacleRegister#NO_OBSTACLE} if none.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(register.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Integer candidate : candidates) {
            double distance = odist(candidate);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }
}
