package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * Sensor providing the AUV's raw environmental data and obstacle queries
 * (LRE-DM5).
 * <p>
 * When called with index -1 (no obstacle exists), distance functions
 * return Double.MAX_VALUE and field accessors return 0.0, so the
 * controller and operation layers can call them unconditionally without
 * sentinel guards.
 */
public final class Sensor {

    private final ObstacleRegister obstacleRegister;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double ns_vel;

    @RoboChartType("real")
    private double ew_vel;

    @RoboChartType("real")
    private double rate_of_climb;

    public Sensor(ObstacleRegister obstacleRegister) {
        this.obstacleRegister = obstacleRegister;
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
    public double nsVel() {
        return ns_vel;
    }

    @RoboChartType("real")
    public double ewVel() {
        return ew_vel;
    }

    @RoboChartType("real")
    public double rateOfClimb() {
        return rate_of_climb;
    }

    public ObstacleRegister obstacles() {
        return obstacleRegister;
    }

    // --- Distance functions ---------------------------------------------------

    /**
     * Horizontal distance to the obstacle at {@code index} (LRE-SF1).
     */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(o.ns_rel_dist() * o.ns_rel_dist() + o.ew_rel_dist() * o.ew_rel_dist());
    }

    /**
     * Vertical distance to the obstacle at {@code index} (LRE-SF2).
     */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return Double.MAX_VALUE;
        }
        return Math.abs(depth - o.obs_depth());
    }

    /**
     * Overall Euclidean distance to the obstacle at {@code index} (LRE-SF3).
     */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return Double.MAX_VALUE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    // --- Field accessors (return 0.0 when index == -1) ------------------------

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return 0.0;
        }
        return o.ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return 0.0;
        }
        return o.ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return 0.0;
        }
        return o.obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        Obstacle o = obstacleRegister.get(index);
        if (o == null) {
            return 0.0;
        }
        return o.obs_ew_vel();
    }

    // --- Selection functions --------------------------------------------------

    /**
     * Index of the closest static obstacle, or -1 if none (LRE-DM5).
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        List<Integer> staticIdx = obstacleRegister.staticIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < staticIdx.size(); i = i + 1) {
            int idx = staticIdx.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }

    /**
     * Index of the closest dynamic obstacle, or -1 if none (LRE-DM5).
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        List<Integer> dynIdx = obstacleRegister.dynamicIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < dynIdx.size(); i = i + 1) {
            int idx = dynIdx.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }
}
