package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * LRE-DM5: Sensor for the AUV environment. Holds raw inputs and an
 * ObstacleRegister. Provides distance functions and selection of the
 * closest static / dynamic obstacle. Returns safe defaults when the
 * index is -1 (no obstacle).
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

    public void update(@RoboChartType("real") double depth,
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

    public ObstacleRegister register() { return register; }

    /** LRE-SF1: horizontal distance to obstacle at index. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.lookup(index);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle obs = o.get();
        return Math.sqrt(obs.ns_rel_dist() * obs.ns_rel_dist()
                       + obs.ew_rel_dist() * obs.ew_rel_dist());
    }

    /** LRE-SF2: vertical distance to obstacle at index. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.lookup(index);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle obs = o.get();
        return Math.abs(depth - obs.obs_depth());
    }

    /** LRE-SF3: overall Euclidean distance to obstacle at index. */
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
        Optional<Obstacle> o = register.lookup(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().obs_ew_vel();
    }

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
