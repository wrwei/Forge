package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Sensor interface providing raw environmental data and derived distance
 * functions (LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3).
 *
 * When called with index -1 (no obstacle), distance functions return
 * Double.MAX_VALUE and obstacle-field accessors return 0.0.
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

    public void update(
            @RoboChartType("real") double depth,
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

    public ObstacleRegister register() { return register; }

    // --- distance functions (LRE-SF1, LRE-SF2, LRE-SF3) ---

    /** Horizontal distance: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
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

    /** Vertical distance: |depth - obs_depth|. */
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

    /** Overall Euclidean distance: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        if (!register.contains(index)) {
            return Double.MAX_VALUE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    // --- obstacle field accessors (return 0.0 when index == -1) ---

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

    /** Returns the index of the nearest static obstacle, or -1 if none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (Integer idx : register.staticIndices()) {
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }

    /** Returns the index of the nearest dynamic obstacle, or -1 if none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (Integer idx : register.dynamicIndices()) {
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }
}
