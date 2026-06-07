package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental sensor (LRE-DM5). Provides raw measurements,
 * distance functions to registered obstacles, obstacle field accessors,
 * and closest static/dynamic obstacle selection. When no obstacle exists
 * at the requested index, distance functions and position accessors
 * return a safe large distance and velocity accessors return zero, so
 * downstream operations and guards need no sentinel checks.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double NO_OBSTACLE_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = ObstacleRegister.empty();

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

    public double depth() {
        return depth;
    }

    public double nsVel() {
        return nsVel;
    }

    public double ewVel() {
        return ewVel;
    }

    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at the given index (LRE-SF1). */
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obs = register.lookup(index);
        return Math.sqrt(obs.nsRelDist() * obs.nsRelDist()
                + obs.ewRelDist() * obs.ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index (LRE-SF2). */
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        Obstacle obs = register.lookup(index);
        return Math.abs(depth - obs.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index (LRE-SF3). */
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index)
                + vdist(index) * vdist(index));
    }

    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return register.lookup(index).nsRelDist();
    }

    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return NO_OBSTACLE_DISTANCE;
        }
        return register.lookup(index).ewRelDist();
    }

    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obsNsVel();
    }

    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).obsEwVel();
    }

    /** Index of the nearest static obstacle, or -1 if none exists. */
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = NO_OBSTACLE_DISTANCE;
        for (int index : register.indices()) {
            if (register.lookup(index).isStatic() && odist(index) < bestDist) {
                best = index;
                bestDist = odist(index);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 if none exists. */
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = NO_OBSTACLE_DISTANCE;
        for (int index : register.indices()) {
            if (register.lookup(index).isDynamic() && odist(index) < bestDist) {
                best = index;
                bestDist = odist(index);
            }
        }
        return best;
    }
}
