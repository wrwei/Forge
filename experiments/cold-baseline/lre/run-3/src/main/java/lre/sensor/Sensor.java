package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * LRE-DM5: Sensor interface providing AUV raw environmental data
 * plus derived obstacle distance and selection functions.
 *
 * Sentinel handling: when called with index -1 (no obstacle exists),
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

    public void setDepth(@RoboChartType("real") double d) { this.depth = d; }
    public void setNsVel(@RoboChartType("real") double v) { this.ns_vel = v; }
    public void setEwVel(@RoboChartType("real") double v) { this.ew_vel = v; }
    public void setRateOfClimb(@RoboChartType("real") double v) { this.rate_of_climb = v; }
    public void setRegister(ObstacleRegister r) { this.register = r; }

    @RoboChartType("real")
    public double depth() { return depth; }

    @RoboChartType("real")
    public double nsVel() { return ns_vel; }

    @RoboChartType("real")
    public double ewVel() { return ew_vel; }

    @RoboChartType("real")
    public double rateOfClimb() { return rate_of_climb; }

    // ---- LRE-SF1: horizontal distance ----
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        if (!register.contains(index)) {
            return Double.MAX_VALUE;
        }
        Obstacle o = register.get(index);
        return Math.sqrt(o.ns_rel_dist() * o.ns_rel_dist() + o.ew_rel_dist() * o.ew_rel_dist());
    }

    // ---- LRE-SF2: vertical distance ----
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        if (!register.contains(index)) {
            return Double.MAX_VALUE;
        }
        Obstacle o = register.get(index);
        return Math.abs(depth - o.obs_depth());
    }

    // ---- LRE-SF3: overall Euclidean distance ----
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        if (!register.contains(index)) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    // ---- Obstacle field accessors (with sentinel handling) ----
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    // ---- Closest static obstacle ----
    public int closestStaticIndex() {
        List<Integer> idx = register.staticIndices();
        if (idx.isEmpty()) {
            return -1;
        }
        int best = idx.get(0);
        double bestDist = odist(best);
        for (int i = 1; i < idx.size(); i++) {
            int candidate = idx.get(i);
            double d = odist(candidate);
            if (d < bestDist) {
                best = candidate;
                bestDist = d;
            }
        }
        return best;
    }

    // ---- Closest dynamic obstacle ----
    public int closestDynamicIndex() {
        List<Integer> idx = register.dynamicIndices();
        if (idx.isEmpty()) {
            return -1;
        }
        int best = idx.get(0);
        double bestDist = odist(best);
        for (int i = 1; i < idx.size(); i++) {
            int candidate = idx.get(i);
            double d = odist(candidate);
            if (d < bestDist) {
                best = candidate;
                bestDist = d;
            }
        }
        return best;
    }
}
