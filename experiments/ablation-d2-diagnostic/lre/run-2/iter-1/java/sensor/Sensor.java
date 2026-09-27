package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the derived obstacle geometry the
 * controller's guards are expressed over (LRE-DM5, LRE-SF1..LRE-SF3).
 *
 * <p>All lookups are total: when the requested identifier is not in the
 * domain of the obstacle register the distance functions return a safe
 * large distance and the obstacle field accessors return zero, so that the
 * controller never has to test for the absence of an obstacle.
 */
public final class Sensor {

    /** Distance reported when no obstacle is mapped to the requested identifier. */
    private static final double SAFE_DISTANCE = 1.0E6;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = new ObstacleRegister();

    /** Records a fresh reading of the AUV's own state. */
    public void update(
            @RoboChartType("real") double depth,
            @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel,
            @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Installs the current obstacle register. */
    public void updateRegister(ObstacleRegister register) {
        this.register = register;
    }

    /** The obstacle register currently held by the sensor. */
    public ObstacleRegister register() {
        return register;
    }

    /** AUV depth below the surface, in metres. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity, in m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at the given index (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(
                register.lookup(index).nsRelDist() * register.lookup(index).nsRelDist()
                        + register.lookup(index).ewRelDist() * register.lookup(index).ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.abs(depth - register.lookup(index).obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the given index. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        return register.lookup(index).nsRelDist();
    }

    /** East-west relative distance of the obstacle at the given index. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        return register.lookup(index).ewRelDist();
    }

    /** North-south velocity of the obstacle at the given index. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        return register.lookup(index).obsNsVel();
    }

    /** East-west velocity of the obstacle at the given index. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        return register.lookup(index).obsEwVel();
    }

    /** Index of the nearest static obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDistance = SAFE_DISTANCE;
        for (var position = 0; position < register.size(); position++) {
            var candidate = register.indexAt(position);
            if (register.isStaticAt(candidate) && odist(candidate) < bestDistance) {
                best = candidate;
                bestDistance = odist(candidate);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 when there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDistance = SAFE_DISTANCE;
        for (var position = 0; position < register.size(); position++) {
            var candidate = register.indexAt(position);
            if (register.isDynamicAt(candidate) && odist(candidate) < bestDistance) {
                best = candidate;
                bestDistance = odist(candidate);
            }
        }
        return best;
    }
}
