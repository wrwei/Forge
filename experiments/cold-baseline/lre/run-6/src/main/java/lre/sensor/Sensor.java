package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Sensor (LRE-DM5): the AUV's raw environmental data plus the obstacle
 * registry. Exposes distance and field-accessor functions, plus
 * selection functions for the closest static / dynamic obstacle.
 *
 * <p>When called with index -1 (no obstacle), the distance functions
 * return Double.MAX_VALUE and the field accessors return 0.0. This
 * lets the controller layer call these methods unconditionally without
 * sentinel guards.
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

    public Sensor() {
        this.register = new ObstacleRegister();
    }

    public Sensor(ObstacleRegister register) {
        this.register = register;
    }

    // --- raw environmental field accessors ---

    @RoboChartType("real")
    public double depth() { return depth; }

    @RoboChartType("real")
    public double ns_vel() { return ns_vel; }

    @RoboChartType("real")
    public double ew_vel() { return ew_vel; }

    @RoboChartType("real")
    public double rate_of_climb() { return rate_of_climb; }

    // --- environmental setters (for the simulation harness) ---

    public void setDepth(@RoboChartType("real") double v) { this.depth = v; }
    public void setNsVel(@RoboChartType("real") double v) { this.ns_vel = v; }
    public void setEwVel(@RoboChartType("real") double v) { this.ew_vel = v; }
    public void setRateOfClimb(@RoboChartType("real") double v) { this.rate_of_climb = v; }

    public ObstacleRegister register() { return register; }

    // --- distance functions ---

    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle obs = o.get();
        return Math.sqrt(obs.ns_rel_dist() * obs.ns_rel_dist() + obs.ew_rel_dist() * obs.ew_rel_dist());
    }

    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle obs = o.get();
        return Math.abs(depth - obs.obs_depth());
    }

    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double h = hdist(idx);
        double v = vdist(idx);
        return Math.sqrt(h * h + v * v);
    }

    // --- per-obstacle field accessors with -1 sentinel handling ---

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int idx) {
        if (idx == -1) {
            return 0.0;
        }
        Optional<Obstacle> o = register.lookup(idx);
        if (o.isEmpty()) {
            return 0.0;
        }
        return o.get().obs_ew_vel();
    }

    // --- selection: closest static / dynamic obstacle ---

    @RoboChartType("nat")
    public int closestStaticIndex() {
        int bestIdx = -1;
        double bestDist = Double.MAX_VALUE;
        List<Integer> idxs = register.staticIndices();
        for (int i = 0; i < idxs.size(); i++) {
            int idx = idxs.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                bestIdx = idx;
            }
        }
        return bestIdx;
    }

    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int bestIdx = -1;
        double bestDist = Double.MAX_VALUE;
        List<Integer> idxs = register.dynamicIndices();
        for (int i = 0; i < idxs.size(); i++) {
            int idx = idxs.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                bestIdx = idx;
            }
        }
        return bestIdx;
    }
}
