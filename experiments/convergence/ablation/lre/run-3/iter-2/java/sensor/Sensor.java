package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental sensor. Provides raw kinematic data, an obstacle
 * register, distance functions, obstacle field accessors, and selection
 * functions. When no obstacle exists at the requested index, distance
 * functions return a safe large distance and field accessors return zero.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double SAFE_DISTANCE = 1.0E9;

    @RoboChartType("real")
    private double depth;
    @RoboChartType("real")
    private double nsVel;
    @RoboChartType("real")
    private double ewVel;
    @RoboChartType("real")
    private double rateOfClimb;
    private ObstacleRegister register = new ObstacleRegister();

    /** Updates the sensor with a fresh environmental reading. */
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

    /** AUV depth below surface, metres. */
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity, m/s. */
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity, m/s. */
    public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity, m/s. */
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at index, metres. */
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obs = register.get(index);
        return Math.sqrt(obs.nsRelDist() * obs.nsRelDist()
                + obs.ewRelDist() * obs.ewRelDist());
    }

    /** Vertical distance to the obstacle at index, metres. */
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obs = register.get(index);
        return Math.abs(depth - obs.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at index, metres. */
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index)
                + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at index, metres. */
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).nsRelDist();
    }

    /** East-west relative distance of the obstacle at index, metres. */
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ewRelDist();
    }

    /** North-south velocity of the obstacle at index, m/s. */
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obsNsVel();
    }

    /** East-west velocity of the obstacle at index, m/s. */
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obsEwVel();
    }

    /** Index of the nearest static obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = SAFE_DISTANCE;
        for (Integer index : register.indices()) {
            Obstacle obs = register.get(index);
            if (obs.isStatic() && odist(index) < bestDist) {
                bestDist = odist(index);
                best = index;
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = SAFE_DISTANCE;
        for (Integer index : register.indices()) {
            Obstacle obs = register.get(index);
            if (!obs.isStatic() && odist(index) < bestDist) {
                bestDist = odist(index);
                best = index;
            }
        }
        return best;
    }

    /**
     * Closest distance of approach to the obstacle at index, metres.
     * Returns the safe large distance when no obstacle exists at index.
     * When the relative velocity is zero the current distance is returned.
     */
    public double closestApproachDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_DISTANCE;
        }
        Obstacle obs = register.get(index);
        double relNsVel = obs.obsNsVel() - nsVel;
        double relEwVel = obs.obsEwVel() - ewVel;
        double relVertVel = rateOfClimb - obs.obsRoc();
        double relVertDist = obs.obsDepth() - depth;
        double closingSq = relNsVel * relNsVel + relEwVel * relEwVel
                + relVertVel * relVertVel;
        if (closingSq == 0.0) {
            return odist(index);
        }
        double t = -(obs.nsRelDist() * relNsVel + obs.ewRelDist() * relEwVel
                + relVertDist * relVertVel) / closingSq;
        if (t < 0.0) {
            t = 0.0;
        }
        double ns = obs.nsRelDist() + relNsVel * t;
        double ew = obs.ewRelDist() + relEwVel * t;
        double vert = relVertDist + relVertVel * t;
        return Math.sqrt(ns * ns + ew * ew + vert * vert);
    }

    /**
     * Time at closest point of approach to the obstacle at index, seconds.
     * Returns zero when no obstacle exists at index or the relative
     * velocity is zero; negative when the obstacle is moving away.
     */
    public double closestApproachTime(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        Obstacle obs = register.get(index);
        double relNsVel = obs.obsNsVel() - nsVel;
        double relEwVel = obs.obsEwVel() - ewVel;
        double relVertVel = rateOfClimb - obs.obsRoc();
        double relVertDist = obs.obsDepth() - depth;
        double closingSq = relNsVel * relNsVel + relEwVel * relEwVel
                + relVertVel * relVertVel;
        if (closingSq == 0.0) {
            return 0.0;
        }
        return -(obs.nsRelDist() * relNsVel + obs.ewRelDist() * relEwVel
                + relVertDist * relVertVel) / closingSq;
    }
}
