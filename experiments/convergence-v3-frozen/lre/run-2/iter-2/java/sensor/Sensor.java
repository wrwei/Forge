package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the derived geometry the LRE reads
 * (LRE-DM5, LRE-SF1 to LRE-SF3).
 *
 * <p>Every index-taking method returns a SAFE DEFAULT for an index that names
 * no obstacle, so the controller's guards never need an existence check:
 * distances come back large and obstacle velocities come back zero.
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

    private ObstacleRegister register = new ObstacleRegister();

    /** Refreshes the AUV's own raw readings for this cycle. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double ns_vel,
                       @RoboChartType("real") double ew_vel,
                       @RoboChartType("real") double rate_of_climb) {
        this.depth = depth;
        this.ns_vel = ns_vel;
        this.ew_vel = ew_vel;
        this.rate_of_climb = rate_of_climb;
    }

    /** Replaces the obstacle register for this cycle. */
    public void setRegister(ObstacleRegister register) {
        this.register = register;
    }

    /** The obstacle register the sensor is currently holding. */
    public ObstacleRegister register() {
        return register;
    }

    /** AUV depth below the surface, metres. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** North-south horizontal velocity, m/s. */
    @RoboChartType("real")
    public double ns_vel() {
        return ns_vel;
    }

    /** East-west horizontal velocity, m/s. */
    @RoboChartType("real")
    public double ew_vel() {
        return ew_vel;
    }

    /** Vertical velocity, m/s. */
    @RoboChartType("real")
    public double rate_of_climb() {
        return rate_of_climb;
    }

    /** LRE-SF1: horizontal distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!known(index)) {
            return 1000.0;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** LRE-SF2: vertical distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!known(index)) {
            return 1000.0;
        }
        Obstacle obstacle = register.get(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** LRE-SF3: overall Euclidean distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** Relative north-south distance; a large default when no obstacle. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!known(index)) {
            return 1000.0;
        }
        return register.get(index).ns_rel_dist();
    }

    /** Relative east-west distance; a large default when no obstacle. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!known(index)) {
            return 1000.0;
        }
        return register.get(index).ew_rel_dist();
    }

    /** Obstacle north-south velocity; zero when no obstacle. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!known(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    /** Obstacle east-west velocity; zero when no obstacle. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!known(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    /**
     * Squared magnitude of the obstacle's velocity relative to the AUV, floored
     * at a small positive value so the closest-point-of-approach division is
     * total even when there is no relative motion.
     */
    @RoboChartType("real")
    public double relSpeedSq(@RoboChartType("nat") int index) {
        var relNs = obsNsVel(index) - ns_vel;
        var relEw = obsEwVel(index) - ew_vel;
        var squared = relNs * relNs + relEw * relEw;
        if (squared < 0.000001) {
            return 0.000001;
        }
        return squared;
    }

    /** Index of the nearest static obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDist = 1000.0;
        for (var i = 0; i < register.size(); i++) {
            if (register.isStatic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDist = 1000.0;
        for (var i = 0; i < register.size(); i++) {
            if (register.isDynamic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }

    private boolean known(int index) {
        return index >= 0 && index < register.size();
    }
}
