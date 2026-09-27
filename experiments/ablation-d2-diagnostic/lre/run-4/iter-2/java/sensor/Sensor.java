package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the derived obstacle
 * quantities the LRE operations consume (LRE-DM5).
 *
 * <p>All lookups return safe defaults when the requested obstacle
 * does not exist, so the controller never needs a sentinel check.
 */
public final class Sensor {

    /** Distance reported when no obstacle exists. */
    @RoboChartType("real")
    private static final double SAFE_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = new ObstacleRegister();

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

    /** Horizontal distance to the obstacle at the given index (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(nsRelDist(index) * nsRelDist(index) + ewRelDist(index) * ewRelDist(index));
    }

    /** Vertical distance to the obstacle at the given index (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.abs(depth - register.get(index).obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).nsRelDist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ewRelDist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obsNsVel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obsEwVel();
    }

    /**
     * Squared relative speed between the AUV and the obstacle at the
     * given index, floored at 1 so that it is never zero. With no
     * relative motion the closest point of approach is the current
     * position, which the floored value reproduces.
     */
    @RoboChartType("real")
    public double relSpeedSq(@RoboChartType("nat") int index) {
        if ((obsNsVel(index) - nsVel) * (obsNsVel(index) - nsVel)
                + (obsEwVel(index) - ewVel) * (obsEwVel(index) - ewVel) <= 0.0) {
            return 1.0;
        }
        return (obsNsVel(index) - nsVel) * (obsNsVel(index) - nsVel)
                + (obsEwVel(index) - ewVel) * (obsEwVel(index) - ewVel);
    }

    /** Index of the nearest static obstacle, or -1 when none exists. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDist = SAFE_DISTANCE;
        for (var i = 0; i < register.size(); i++) {
            if (register.isStatic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 when none exists. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDist = SAFE_DISTANCE;
        for (var i = 0; i < register.size(); i++) {
            if (register.isDynamic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }
}
