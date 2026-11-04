package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle quantities derived from
 * it (LRE-DM5, LRE-SF1..3). Indices refer to the {@link ObstacleRegister};
 * for an index with no obstacle the distance functions return a safe large
 * distance and the field accessors return zero.
 */
public final class Sensor {

    // Distance reported for an index with no obstacle: far beyond every threshold.
    @RoboChartType("real") private static final double NO_OBSTACLE_DIST = 1000.0;

    private final ObstacleRegister obstacles;
    @RoboChartType("real") private double depth = 0.0;
    @RoboChartType("real") private double nsVel = 0.0;
    @RoboChartType("real") private double ewVel = 0.0;
    @RoboChartType("real") private double rateOfClimb = 0.0;

    /** Creates a sensor over the given obstacle register. */
    public Sensor(ObstacleRegister obstacles) {
        this.obstacles = obstacles;
    }

    /** Records a new reading of the AUV's own state. */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth, metres below the surface. */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity, m/s. */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity, m/s. */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV rate of climb (vertical velocity), m/s. */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at {@code index} (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        Obstacle obstacle = obstacles.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index} (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        return Math.abs(depth - obstacles.get(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index} (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return NO_OBSTACLE_DIST;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    /** North-south relative distance of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least odist, or {@link ObstacleRegister#NO_OBSTACLE}. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /** Index of the dynamic obstacle with the least odist, or {@link ObstacleRegister#NO_OBSTACLE}. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // The candidate with the least odist; the lowest index wins a tie.
    private int closestOf(List<Integer> candidates) {
        int best = ObstacleRegister.NO_OBSTACLE;
        double bestDist = 0.0;
        boolean found = false;
        for (int index : candidates) {
            double d = odist(index);
            if (!found || d < bestDist) {
                best = index;
                bestDist = d;
                found = true;
            }
        }
        return best;
    }
}
