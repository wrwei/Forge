package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Sensor interface of the AUV (LRE-DM5). Provides raw environmental data,
 * an obstacle register, distance functions, obstacle field accessors, and
 * closest static/dynamic obstacle selection. When no obstacle exists at
 * the requested index, distance functions return a safe large distance and
 * field accessors return zero.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double SAFE_LARGE_DISTANCE = 1.0e9;

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
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    public void setRegister(ObstacleRegister register) {
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

    /** Horizontal distance to the obstacle at the index (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obs = register.lookup(index).get();
        return Math.sqrt(obs.nsRelDist() * obs.nsRelDist() + obs.ewRelDist() * obs.ewRelDist());
    }

    /** Vertical distance to the obstacle at the index (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obs = register.lookup(index).get();
        return Math.abs(depth - obs.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the index (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the index. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().nsRelDist();
    }

    /** East-west relative distance of the obstacle at the index. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().ewRelDist();
    }

    /** North-south velocity of the obstacle at the index. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().obsNsVel();
    }

    /** East-west velocity of the obstacle at the index. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().obsEwVel();
    }

    /** Index of the nearest static obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = SAFE_LARGE_DISTANCE;
        for (Integer key : register.indices()) {
            int index = key.intValue();
            Obstacle obs = register.lookup(index).get();
            if (obs.isStatic() && odist(index) < bestDist) {
                best = index;
                bestDist = odist(index);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = SAFE_LARGE_DISTANCE;
        for (Integer key : register.indices()) {
            int index = key.intValue();
            Obstacle obs = register.lookup(index).get();
            if (obs.isDynamic() && odist(index) < bestDist) {
                best = index;
                bestDist = odist(index);
            }
        }
        return best;
    }
}
