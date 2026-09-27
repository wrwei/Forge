package lre.sensor;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;

/**
 * Raw environmental data for the AUV plus the derived distance and selection
 * functions used by the LRE operations and guards (LRE-DM5).
 *
 * <p>When no obstacle exists at a given index the distance functions return a
 * safe large distance and the obstacle velocity accessors return zero, so that
 * controller guards never need a sentinel check.</p>
 */
public final class Sensor {

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = new ObstacleRegister();

    public void update(
            @RoboChartType("real") double depth,
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

    /** Horizontal distance to the obstacle at {@code index} (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.sqrt(obstacle.nsRelDist() * obstacle.nsRelDist()
                + obstacle.ewRelDist() * obstacle.ewRelDist());
    }

    /** Vertical distance to the obstacle at {@code index} (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        Obstacle obstacle = register.get(index);
        return Math.abs(depth - obstacle.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index} (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
        }
        return register.get(index).nsRelDist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return LreConstants.SAFE_LARGE_DIST;
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

    /** Index of the nearest static obstacle, or -1 when none exists. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < register.size(); i++) {
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
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < register.size(); i++) {
            if (register.isDynamic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }
}
