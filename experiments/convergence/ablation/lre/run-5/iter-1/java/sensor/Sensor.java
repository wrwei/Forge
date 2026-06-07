package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Environmental sensor interface of the AUV. Provides raw environmental
 * data, distance functions, obstacle field accessors, and closest-obstacle
 * selection. When no obstacle exists at the requested index, distance
 * functions return a safe large distance and field accessors return zero.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double SAFE_LARGE_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = new ObstacleRegister();

    /** Updates the raw environmental readings. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Replaces the obstacle register. */
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

    /** Horizontal distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obs = register.lookup(index).get();
        return Math.sqrt(obs.nsRelDist() * obs.nsRelDist() + obs.ewRelDist() * obs.ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obs = register.lookup(index).get();
        return Math.abs(depth - obs.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index. */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the given index. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().nsRelDist();
    }

    /** East-west relative distance of the obstacle at the given index. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().ewRelDist();
    }

    /** North-south velocity of the obstacle at the given index. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return 0.0;
        }
        return register.lookup(index).get().obsNsVel();
    }

    /** East-west velocity of the obstacle at the given index. */
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
        double bestDist = 0.0;
        for (Integer idx : register.staticIndices()) {
            double d = odist(idx.intValue());
            if (best == -1 || d < bestDist) {
                best = idx.intValue();
                bestDist = d;
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = 0.0;
        for (Integer idx : register.dynamicIndices()) {
            double d = odist(idx.intValue());
            if (best == -1 || d < bestDist) {
                best = idx.intValue();
                bestDist = d;
            }
        }
        return best;
    }

    /**
     * Closest distance of approach to the obstacle at the given index.
     * Returns the safe large distance when no obstacle exists at the
     * index, and the current horizontal distance when there is no
     * relative motion or the closest approach is already past.
     */
    @RoboChartType("real")
    public double cda(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        double relNsVel = obsNsVel(index) - nsVel;
        double relEwVel = obsEwVel(index) - ewVel;
        double closingSq = relNsVel * relNsVel + relEwVel * relEwVel;
        if (closingSq == 0.0) {
            return hdist(index);
        }
        double t = -((nsRelDist(index) * relNsVel + ewRelDist(index) * relEwVel) / closingSq);
        if (t < 0.0) {
            return hdist(index);
        }
        double dNs = nsRelDist(index) + relNsVel * t;
        double dEw = ewRelDist(index) + relEwVel * t;
        return Math.sqrt(dNs * dNs + dEw * dEw);
    }

    /**
     * Time at closest point of approach to the obstacle at the given
     * index. Returns -1 when no obstacle exists at the index or there is
     * no relative motion (the obstacle never approaches).
     */
    @RoboChartType("real")
    public double tcpa(@RoboChartType("nat") int index) {
        if (!register.contains(index)) {
            return -1.0;
        }
        double relNsVel = obsNsVel(index) - nsVel;
        double relEwVel = obsEwVel(index) - ewVel;
        double closingSq = relNsVel * relNsVel + relEwVel * relEwVel;
        if (closingSq == 0.0) {
            return -1.0;
        }
        return -((nsRelDist(index) * relNsVel + ewRelDist(index) * relEwVel) / closingSq);
    }
}
