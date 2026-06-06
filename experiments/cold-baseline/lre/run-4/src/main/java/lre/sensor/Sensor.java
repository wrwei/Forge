package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Sensor providing AUV environmental data and obstacle queries. See LRE-DM5.
 *
 * Sentinel handling: when callers pass index == -1 (no obstacle exists),
 * distance functions return Double.MAX_VALUE and field accessors return 0.0.
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
        this.depth = 0.0;
        this.ns_vel = 0.0;
        this.ew_vel = 0.0;
        this.rate_of_climb = 0.0;
        this.register = new ObstacleRegister();
    }

    public void updateDepth(@RoboChartType("real") double depth) {
        this.depth = depth;
    }

    public void updateVelocity(@RoboChartType("real") double ns_vel,
                               @RoboChartType("real") double ew_vel,
                               @RoboChartType("real") double rate_of_climb) {
        this.ns_vel = ns_vel;
        this.ew_vel = ew_vel;
        this.rate_of_climb = rate_of_climb;
    }

    public void updateRegister(ObstacleRegister register) {
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

    // --- Obstacle field accessors ---

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.get(index);
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
        Optional<Obstacle> o = register.get(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsDepth(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.get(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().obs_depth();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.get(index);
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
        Optional<Obstacle> o = register.get(index);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().obs_ew_vel();
    }

    // --- Distance functions (LRE-SF1..3) ---

    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.get(index);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double ns = o.get().ns_rel_dist();
        double ew = o.get().ew_rel_dist();
        return Math.sqrt(ns * ns + ew * ew);
    }

    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.get(index);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        return Math.abs(depth - o.get().obs_depth());
    }

    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.get(index);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    // --- Selection functions ---

    @RoboChartType("nat")
    public int closestStaticIndex() {
        List<Integer> indices = register.staticIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
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
        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }
}
