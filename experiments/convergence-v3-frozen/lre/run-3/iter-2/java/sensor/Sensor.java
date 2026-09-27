package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data together with the derived distances and
 * obstacle selections the Last Response Engine depends on.
 *
 * <p>Every accessor is total: when no obstacle is registered at an index the
 * distance functions report a safe large distance and the field accessors
 * report zero, so that callers never need a sentinel check.</p>
 */
public final class Sensor {

    /** Reported when no obstacle exists, so that proximity guards stay false. */
    @RoboChartType("real")
    private static final double SAFE_LARGE_DIST = 1000.0;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = new ObstacleRegister();

    /** Replaces the AUV's raw state for the current control cycle. */
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

    /** Replaces the obstacle register for the current control cycle. */
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

    /** Horizontal distance to the obstacle at the given index, in metres. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.sqrt(register.get(index).nsRelDist() * register.get(index).nsRelDist()
                + register.get(index).ewRelDist() * register.get(index).ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index, in metres. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.abs(depth - register.get(index).obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index, in metres. */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the given index, in metres. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).nsRelDist();
    }

    /** East-west relative distance of the obstacle at the given index, in metres. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ewRelDist();
    }

    /** North-south velocity of the obstacle at the given index, in m/s. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obsNsVel();
    }

    /** East-west velocity of the obstacle at the given index, in m/s. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obsEwVel();
    }

    /** Index of the nearest static obstacle by overall distance, or -1 if there is none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var closest = -1;
        var closestDist = SAFE_LARGE_DIST;
        for (var i = 0; i < register.size(); i++) {
            if (register.isStatic(i) && odist(i) < closestDist) {
                closest = i;
                closestDist = odist(i);
            }
        }
        return closest;
    }

    /** Index of the nearest dynamic obstacle by overall distance, or -1 if there is none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var closest = -1;
        var closestDist = SAFE_LARGE_DIST;
        for (var i = 0; i < register.size(); i++) {
            if (register.isDynamic(i) && odist(i) < closestDist) {
                closest = i;
                closestDist = odist(i);
            }
        }
        return closest;
    }
}
