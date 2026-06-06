package lre.sensor;

/**
 * Sensor interface providing the AUV's raw environmental data, obstacle
 * distances, obstacle field accessors, and closest-obstacle selection
 * (LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3).
 *
 * <p>When no obstacle exists at a requested index, the distance functions
 * return a safe large distance and the field accessors return zero.
 */
public final class Sensor {

    /** Safe large distance returned when no obstacle exists. */
    private static final double NO_OBSTACLE_DIST = 1000000.0;

    private double depth;
    private double nsVel;
    private double ewVel;
    private double rateOfClimb;
    private ObstacleRegister obstacles = new ObstacleRegister();

    /** Updates the AUV's raw environmental readings. */
    public void update(double depthValue, double nsVelValue, double ewVelValue, double rocValue) {
        this.depth = depthValue;
        this.nsVel = nsVelValue;
        this.ewVel = ewVelValue;
        this.rateOfClimb = rocValue;
    }

    /** Replaces the obstacle register. */
    public void updateObstacles(ObstacleRegister register) {
        this.obstacles = register;
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

    /** AUV vertical velocity (rate of climb), m/s. */
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at the given index (LRE-SF1). */
    public double hdist(int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle o = found.get();
        return Math.sqrt(o.nsRelDist() * o.nsRelDist() + o.ewRelDist() * o.ewRelDist());
    }

    /** Vertical distance to the obstacle at the given index (LRE-SF2). */
    public double vdist(int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle o = found.get();
        return Math.abs(depth - o.obsDepth());
    }

    /** Overall Euclidean distance to the obstacle at the given index (LRE-SF3). */
    public double odist(int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the given index. */
    public double nsRelDist(int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().nsRelDist();
    }

    /** East-west relative distance of the obstacle at the given index. */
    public double ewRelDist(int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().ewRelDist();
    }

    /** North-south velocity of the obstacle at the given index. */
    public double obsNsVel(int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().obsNsVel();
    }

    /** East-west velocity of the obstacle at the given index. */
    public double obsEwVel(int index) {
        var found = obstacles.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().obsEwVel();
    }

    /** Index of the nearest static obstacle, or -1 if none exists. */
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = NO_OBSTACLE_DIST;
        for (int index : obstacles.indices()) {
            Obstacle o = obstacles.lookup(index).get();
            if (o.isStatic() && odist(index) < bestDist) {
                best = index;
                bestDist = odist(index);
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 if none exists. */
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = NO_OBSTACLE_DIST;
        for (int index : obstacles.indices()) {
            Obstacle o = obstacles.lookup(index).get();
            if (o.isDynamic() && odist(index) < bestDist) {
                best = index;
                bestDist = odist(index);
            }
        }
        return best;
    }
}
