package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Sensor interface (LRE-DM5). Holds raw AUV environmental data and an ObstacleRegister.
 * Provides distance functions, obstacle field accessors, and selection functions.
 *
 * When called with index -1 (no obstacle exists), distance functions return Double.MAX_VALUE
 * (a safe large distance), and field accessors return 0.0. This is the sentinel handling
 * required so that controller predicates can call sensor methods directly without
 * existence checks.
 */
public final class Sensor {

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double ns_vel;

    @RoboChartType("real")
    private double ew_vel;

    @RoboChartType("real")
    private double rate_of_climb;

    private final ObstacleRegister register;

    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double ns_vel,
                       @RoboChartType("real") double ew_vel,
                       @RoboChartType("real") double rate_of_climb) {
        this.depth = depth;
        this.ns_vel = ns_vel;
        this.ew_vel = ew_vel;
        this.rate_of_climb = rate_of_climb;
    }

    @RoboChartType("real")
    public double depth() { return depth; }

    @RoboChartType("real")
    public double nsVel() { return ns_vel; }

    @RoboChartType("real")
    public double ewVel() { return ew_vel; }

    @RoboChartType("real")
    public double rateOfClimb() { return rate_of_climb; }

    /** Horizontal distance (LRE-SF1): sqrt(ns_rel^2 + ew_rel^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> obs = register.lookup(idx);
        if (obs.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle o = obs.get();
        return Math.sqrt(o.ns_rel_dist() * o.ns_rel_dist() + o.ew_rel_dist() * o.ew_rel_dist());
    }

    /** Vertical distance (LRE-SF2): |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> obs = register.lookup(idx);
        if (obs.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle o = obs.get();
        return Math.abs(this.depth - o.obs_depth());
    }

    /** Overall Euclidean distance (LRE-SF3): sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return Double.MAX_VALUE;
        }
        double h = hdist(idx);
        double v = vdist(idx);
        return Math.sqrt(h * h + v * v);
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> obs = register.lookup(idx);
        if (obs.isEmpty()) {
            return 0.0;
        }
        return obs.get().ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> obs = register.lookup(idx);
        if (obs.isEmpty()) {
            return 0.0;
        }
        return obs.get().ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> obs = register.lookup(idx);
        if (obs.isEmpty()) {
            return 0.0;
        }
        return obs.get().obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> obs = register.lookup(idx);
        if (obs.isEmpty()) {
            return 0.0;
        }
        return obs.get().obs_ew_vel();
    }

    /** Index of the closest static obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        List<Integer> indices = register.staticIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (Integer idx : indices) {
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }

    /** Index of the closest dynamic obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        List<Integer> indices = register.dynamicIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (Integer idx : indices) {
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }
}
