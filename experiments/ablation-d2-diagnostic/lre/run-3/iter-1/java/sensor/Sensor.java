package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Raw AUV environmental data plus the derived distance, field-accessor and
 * obstacle-selection functions the LRE operations and guards read.
 *
 * <p>Every accessor returns a safe default when the requested obstacle does not
 * exist: distances yield a large distance, field accessors yield zero. Callers
 * therefore never perform sentinel checks.</p>
 */
public final class Sensor {

    private static final double SAFE_LARGE_DIST = 1000.0;
    private static final double MIN_REL_SPEED_SQ = 0.000001;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register;

    public Sensor() {
        this.register = new ObstacleRegister();
    }

    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb,
                       ObstacleRegister register) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
        this.register = register;
    }

    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    public ObstacleRegister register() {
        return register;
    }

    /** Horizontal distance: sqrt(ns_rel_dist^2 + ew_rel_dist^2). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.at(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance: |depth - obs_depth|. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.abs(depth - register.at(index).obs_depth());
    }

    /** Overall Euclidean distance: sqrt(hdist^2 + vdist^2). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).obs_ew_vel();
    }

    /** North-south velocity of the obstacle relative to the AUV. */
    @RoboChartType("real")
    public double relNsVel(@RoboChartType("nat") int index) {
        return obsNsVel(index) - nsVel;
    }

    /** East-west velocity of the obstacle relative to the AUV. */
    @RoboChartType("real")
    public double relEwVel(@RoboChartType("nat") int index) {
        return obsEwVel(index) - ewVel;
    }

    /**
     * Squared relative speed, floored at a small positive value so that the
     * closest-point-of-approach division is always defined.
     */
    @RoboChartType("real")
    public double relSpeedSq(@RoboChartType("nat") int index) {
        return Math.max(relNsVel(index) * relNsVel(index) + relEwVel(index) * relEwVel(index),
                MIN_REL_SPEED_SQ);
    }

    /** Index of the nearest static obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < register.size(); i++) {
            if (register.isStaticAt(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < register.size(); i++) {
            if (register.isDynamicAt(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }
}
