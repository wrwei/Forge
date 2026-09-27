package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Raw AUV environmental data plus the derived distance and selection functions
 * the LRE operations depend on. Every accessor returns a safe default when the
 * requested obstacle does not exist, so no caller needs a sentinel check.
 */
public final class Sensor {

    /** Distance reported when no obstacle exists; far beyond any threshold. */
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

    private ObstacleRegister register = ObstacleRegister.empty();

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

    /** Horizontal distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_DISTANCE;
        }
        var obstacle = register.at(index);
        return Math.sqrt(obstacle.nsRelDist() * obstacle.nsRelDist()
                + obstacle.ewRelDist() * obstacle.ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_DISTANCE;
        }
        var obstacle = register.at(index);
        return Math.abs(depth - obstacle.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).nsRelDist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).ewRelDist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).obsNsVel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.has(index)) {
            return 0.0;
        }
        return register.at(index).obsEwVel();
    }

    /** Index of the nearest static obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDist = 0.0;
        for (var i = 0; i < register.size(); i++) {
            if (register.isStatic(i)) {
                if (best == -1 || odist(i) < bestDist) {
                    best = i;
                    bestDist = odist(i);
                }
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDist = 0.0;
        for (var i = 0; i < register.size(); i++) {
            if (register.isDynamic(i)) {
                if (best == -1 || odist(i) < bestDist) {
                    best = i;
                    bestDist = odist(i);
                }
            }
        }
        return best;
    }
}
