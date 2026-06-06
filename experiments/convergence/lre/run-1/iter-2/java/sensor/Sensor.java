package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * The AUV's sensor interface (LRE-DM5): raw environmental data, the
 * obstacle register, distance functions, obstacle field accessors, and
 * closest-obstacle selection. When no obstacle exists at a queried
 * index, distance functions return a safe large distance and field
 * accessors return zero.
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
    private ObstacleRegister obstacles = ObstacleRegister.empty();

    /** Updates the AUV's raw environmental readings. */
    public void updateEnvironment(
            @RoboChartType("real") double depth,
            @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel,
            @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Replaces the obstacle register with the latest observation. */
    public void updateObstacles(ObstacleRegister obstacles) {
        this.obstacles = obstacles;
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

    /** Horizontal distance to the obstacle at the given index (LRE-SF1). */
    public double hdist(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = found.get();
        return Math.sqrt(obstacle.nsRelDist() * obstacle.nsRelDist()
                + obstacle.ewRelDist() * obstacle.ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index (LRE-SF2). */
    public double vdist(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = found.get();
        return Math.abs(depth - obstacle.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index (LRE-SF3). */
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the given index, or zero. */
    public double nsRelDist(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().nsRelDist();
    }

    /** East-west relative distance of the obstacle at the given index, or zero. */
    public double ewRelDist(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().ewRelDist();
    }

    /** North-south velocity of the obstacle at the given index, or zero. */
    public double obsNsVel(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().obsNsVel();
    }

    /** East-west velocity of the obstacle at the given index, or zero. */
    public double obsEwVel(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().obsEwVel();
    }

    /** Index of the nearest static obstacle by overall distance, or -1 if none. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        var best = -1;
        var bestDist = 0.0;
        for (Integer index : obstacles.staticIndices()) {
            double candidate = odist(index);
            if (best == -1 || candidate < bestDist) {
                best = index;
                bestDist = candidate;
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle by overall distance, or -1 if none. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        var best = -1;
        var bestDist = 0.0;
        for (Integer index : obstacles.dynamicIndices()) {
            double candidate = odist(index);
            if (best == -1 || candidate < bestDist) {
                best = index;
                bestDist = candidate;
            }
        }
        return best;
    }

    /**
     * Time at closest point of approach between the AUV and the obstacle
     * at the given index, seconds. Computed in three dimensions from the
     * relative position and relative velocity. Returns zero when no
     * obstacle exists or the relative velocity is zero.
     */
    public double tcpaTo(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        Obstacle obstacle = found.get();
        double relVelNs = obstacle.obsNsVel() - nsVel;
        double relVelEw = obstacle.obsEwVel() - ewVel;
        double relVelVert = obstacle.obsRoc() - rateOfClimb;
        double relPosVert = obstacle.obsDepth() - depth;
        double speedSq = relVelNs * relVelNs + relVelEw * relVelEw + relVelVert * relVelVert;
        if (speedSq == 0.0) {
            return 0.0;
        }
        double dot = obstacle.nsRelDist() * relVelNs
                + obstacle.ewRelDist() * relVelEw
                + relPosVert * relVelVert;
        return -dot / speedSq;
    }

    /**
     * Closest distance of approach between the AUV and the obstacle at
     * the given index, metres. The time of closest approach is clamped
     * at zero so a receding obstacle reports its current distance.
     * Returns a safe large distance when no obstacle exists.
     */
    public double cdaTo(@RoboChartType("nat") int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = found.get();
        double time = tcpaTo(index);
        if (time < 0.0) {
            time = 0.0;
        }
        double ns = obstacle.nsRelDist() + (obstacle.obsNsVel() - nsVel) * time;
        double ew = obstacle.ewRelDist() + (obstacle.obsEwVel() - ewVel) * time;
        double vert = (obstacle.obsDepth() - depth) + (obstacle.obsRoc() - rateOfClimb) * time;
        return Math.sqrt(ns * ns + ew * ew + vert * vert);
    }
}
