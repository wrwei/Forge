package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Raw environmental data of the AUV plus the derived distance and selection
 * functions the controller's guards read (LRE-DM5, LRE-SF1..LRE-SF3).
 *
 * <p>Every accessor returns a safe default when the requested obstacle does
 * not exist, so that controller guards never need an existence check.
 */
public final class Sensor {

    /** Distance reported when no obstacle exists at the requested index. */
    @RoboChartType("real")
    private static final double SAFE_LARGE_DIST = 1000.0;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double ns_vel;

    @RoboChartType("real")
    private double ew_vel;

    @RoboChartType("real")
    private double rate_of_climb;

    private ObstacleRegister register = new ObstacleRegister();

    /** Refreshes the AUV's raw readings for the current cycle. */
    public void update(
            @RoboChartType("real") double newDepth,
            @RoboChartType("real") double newNsVel,
            @RoboChartType("real") double newEwVel,
            @RoboChartType("real") double newRateOfClimb) {
        this.depth = newDepth;
        this.ns_vel = newNsVel;
        this.ew_vel = newEwVel;
        this.rate_of_climb = newRateOfClimb;
    }

    /** Installs the obstacle register for the current cycle. */
    public void setRegister(ObstacleRegister newRegister) {
        this.register = newRegister;
    }

    /** The obstacle register currently held. */
    public ObstacleRegister register() {
        return register;
    }

    /** AUV depth below the surface, in metres. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** North-south horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return ns_vel;
    }

    /** East-west horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ew_vel;
    }

    /** Vertical velocity, in m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rate_of_climb;
    }

    /** Horizontal distance to the obstacle at {@code index} (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.obstacleAt(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index} (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.obstacleAt(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index} (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at {@code index}, zero when absent. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return 0.0;
        }
        return register.obstacleAt(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}, zero when absent. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return 0.0;
        }
        return register.obstacleAt(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, zero when absent. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return 0.0;
        }
        return register.obstacleAt(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, zero when absent. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return 0.0;
        }
        return register.obstacleAt(index).obs_ew_vel();
    }

    /**
     * Time at the closest point of approach to the obstacle at {@code index},
     * in seconds; zero when the obstacle is absent or the relative velocity
     * vanishes, in which case the separation never changes.
     */
    @RoboChartType("real")
    public double tcpa(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return 0.0;
        }
        double relNsVel = obsNsVel(index) - ns_vel;
        double relEwVel = obsEwVel(index) - ew_vel;
        double relSpeedSq = relNsVel * relNsVel + relEwVel * relEwVel;
        if (relSpeedSq == 0.0) {
            return 0.0;
        }
        double closingRate = nsRelDist(index) * relNsVel + ewRelDist(index) * relEwVel;
        return (0.0 - closingRate) / relSpeedSq;
    }

    /**
     * Closest distance of approach to the obstacle at {@code index}, in metres;
     * a safe large distance when the obstacle is absent, and the present
     * horizontal distance when the relative velocity vanishes.
     */
    @RoboChartType("real")
    public double cda(@RoboChartType("nat") int index) {
        if (!register.containsIndex(index)) {
            return SAFE_LARGE_DIST;
        }
        double relNsVel = obsNsVel(index) - ns_vel;
        double relEwVel = obsEwVel(index) - ew_vel;
        double relSpeedSq = relNsVel * relNsVel + relEwVel * relEwVel;
        if (relSpeedSq == 0.0) {
            return hdist(index);
        }
        double closingRate = nsRelDist(index) * relNsVel + ewRelDist(index) * relEwVel;
        double perpendicularSq = hdist(index) * hdist(index)
                - closingRate * closingRate / relSpeedSq;
        if (perpendicularSq <= 0.0) {
            return 0.0;
        }
        return Math.sqrt(perpendicularSq);
    }

    /** Index of the nearest static obstacle by overall distance, or -1 if there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int closest = -1;
        double closestDist = Double.MAX_VALUE;
        for (int i = 0; i < register.size(); i++) {
            if (register.isStaticAt(i)) {
                double candidate = odist(i);
                if (candidate < closestDist) {
                    closestDist = candidate;
                    closest = i;
                }
            }
        }
        return closest;
    }

    /** Index of the nearest dynamic obstacle by overall distance, or -1 if there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int closest = -1;
        double closestDist = Double.MAX_VALUE;
        for (int i = 0; i < register.size(); i++) {
            if (register.isDynamicAt(i)) {
                double candidate = odist(i);
                if (candidate < closestDist) {
                    closestDist = candidate;
                    closest = i;
                }
            }
        }
        return closest;
    }
}
