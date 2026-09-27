package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Provides the AUV's raw environmental data, distance functions, obstacle
 * field accessors, and selection functions for the closest static and
 * dynamic obstacles.
 *
 * <p>When called with sentinel index {@code -1}, distance functions return
 * {@link Double#MAX_VALUE} (safe large distance), and field accessors return
 * {@code 0.0}. This keeps controller predicates free of sentinel checks.
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
        this.depth = 0.0;
        this.ns_vel = 0.0;
        this.ew_vel = 0.0;
        this.rate_of_climb = 0.0;
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
    public double depth() {
        return depth;
    }

    @RoboChartType("real")
    public double ns_vel() {
        return ns_vel;
    }

    @RoboChartType("real")
    public double ew_vel() {
        return ew_vel;
    }

    @RoboChartType("real")
    public double rate_of_climb() {
        return rate_of_climb;
    }

    public ObstacleRegister register() {
        return register;
    }

    // ---- distance functions ----

    /**
     * Horizontal distance to the obstacle at {@code index}, sqrt(ns^2 + ew^2).
     * Returns {@link Double#MAX_VALUE} when {@code index == -1} or unknown.
     */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle o = opt.get();
        return Math.sqrt(o.ns_rel_dist() * o.ns_rel_dist() + o.ew_rel_dist() * o.ew_rel_dist());
    }

    /**
     * Vertical distance to the obstacle at {@code index}, |depth - obs_depth|.
     * Returns {@link Double#MAX_VALUE} when {@code index == -1} or unknown.
     */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index != -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle o = opt.get();
        return Math.abs(this.depth - o.obs_depth());
    }

    /**
     * Overall Euclidean distance to the obstacle at {@code index},
     * sqrt(hdist^2 + vdist^2). Returns {@link Double#MAX_VALUE} when
     * {@code index == -1} or unknown.
     */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    // ---- obstacle field accessors ----

    /**
     * Relative north-south distance to the obstacle at {@code index}. Per
     * LRE-OP5, returns a LARGE-distance default when no obstacle exists
     * (index == -1 sentinel or absent). Returning 0 here would let CalcCPA
     * compute a CPA of (0, 0) when the AUV is moving in open water with no
     * dynamic obstacle, spuriously firing the MOM/HCM→CAM transition.
     */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        return opt.get().ns_rel_dist();
    }

    /**
     * Relative east-west distance to the obstacle at {@code index}. Per
     * LRE-OP5, returns a LARGE-distance default when no obstacle exists.
     * See {@link #nsRelDist(int)} for the CalcCPA-spurious-trigger rationale.
     */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Optional<Obstacle> opt = register.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        return opt.get().ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Optional<Obstacle> opt = register.get(index);
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
        Optional<Obstacle> opt = register.get(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().obs_ew_vel();
    }

    // ---- selection functions ----

    /**
     * Returns the index of the static obstacle with the smallest overall
     * distance, or {@code -1} if none exists.
     */
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

    /**
     * Returns the index of the dynamic obstacle with the smallest overall
     * distance, or {@code -1} if none exists.
     */
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
