package lre.sensor;

import lre.annotation.RoboChartType;

/** Raw AUV environmental data plus the derived distance and selection functions. */
public final class Sensor {

    /** Value returned by every distance function when no obstacle exists. */
    @RoboChartType("real")
    private static final double SAFE_LARGE_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double ns_vel;

    @RoboChartType("real")
    private double ew_vel;

    @RoboChartType("real")
    private double rate_of_climb;

    private ObstacleRegister register = new ObstacleRegister();

    /** Installs one cycle of raw sensor data. */
    public void update(
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

    public ObstacleRegister register() {
        return register;
    }

    /** Horizontal distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = register.get(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return register.get(index).ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return register.get(index).ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /** Index of the nearest static obstacle by odist, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDist = SAFE_LARGE_DISTANCE;
        for (var i = 0; i < register.size(); i++) {
            if (register.isStatic(i) && odist(i) < bestDist) {
                bestDist = odist(i);
                best = i;
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle by odist, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDist = SAFE_LARGE_DISTANCE;
        for (var i = 0; i < register.size(); i++) {
            if (register.isDynamic(i) && odist(i) < bestDist) {
                bestDist = odist(i);
                best = i;
            }
        }
        return best;
    }
}
