package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Raw environmental data of the AUV together with the obstacle register and
 * the derived distance and selection functions over it.
 *
 * <p>Every accessor returns a safe default when the requested obstacle does
 * not exist: the distance functions return a large distance and the field
 * accessors return zero. Callers therefore never need an existence check.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double NO_OBSTACLE_DIST = 1000.0;

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

    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        var obstacle = register.get(index);
        return Math.sqrt(obstacle.nsRelDist() * obstacle.nsRelDist()
                + obstacle.ewRelDist() * obstacle.ewRelDist());
    }

    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        var obstacle = register.get(index);
        return Math.abs(depth - obstacle.obsDepth());
    }

    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DIST;
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

    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDist = NO_OBSTACLE_DIST;
        for (var i = 0; i < register.size(); i++) {
            if (register.isStatic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }

    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDist = NO_OBSTACLE_DIST;
        for (var i = 0; i < register.size(); i++) {
            if (register.isDynamic(i) && odist(i) < bestDist) {
                best = i;
                bestDist = odist(i);
            }
        }
        return best;
    }
}
