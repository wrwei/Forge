package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * LRE-DM5: Provides raw environmental data and derived distance / selection
 * functions. Returns safe defaults when called with index -1.
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

    private ObstacleRegister register;

    public Sensor() {
        this.register = new ObstacleRegister();
    }

    public void updateState(
            @RoboChartType("real") double depth,
            @RoboChartType("real") double ns_vel,
            @RoboChartType("real") double ew_vel,
            @RoboChartType("real") double rate_of_climb,
            ObstacleRegister register) {
        this.depth = depth;
        this.ns_vel = ns_vel;
        this.ew_vel = ew_vel;
        this.rate_of_climb = rate_of_climb;
        this.register = register;
    }

    @RoboChartType("real")
    public double depth() { return depth; }

    @RoboChartType("real")
    public double ns_vel() { return ns_vel; }

    @RoboChartType("real")
    public double ew_vel() { return ew_vel; }

    @RoboChartType("real")
    public double rate_of_climb() { return rate_of_climb; }

    /** LRE-SF1: horizontal distance = sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.lookup(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle o = opt.get();
        return Math.sqrt(o.ns_rel_dist() * o.ns_rel_dist() + o.ew_rel_dist() * o.ew_rel_dist());
    }

    /** LRE-SF2: vertical distance = |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.lookup(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle o = opt.get();
        return Math.abs(depth - o.obs_depth());
    }

    /** LRE-SF3: overall Euclidean distance = sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> opt = register.lookup(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> opt = register.lookup(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> opt = register.lookup(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> opt = register.lookup(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().obs_ew_vel();
    }

    /** Index of the static obstacle with minimum odist, or -1 if none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        List<Integer> ids = register.staticIds();
        int bestIdx = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            double d = odist(id);
            if (d < bestDist) {
                bestDist = d;
                bestIdx = id;
            }
        }
        return bestIdx;
    }

    /** Index of the dynamic obstacle with minimum odist, or -1 if none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        List<Integer> ids = register.dynamicIds();
        int bestIdx = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            double d = odist(id);
            if (d < bestDist) {
                bestDist = d;
                bestIdx = id;
            }
        }
        return bestIdx;
    }
}
