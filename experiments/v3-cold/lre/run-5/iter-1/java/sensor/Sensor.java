package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and the obstacle register, with the
 * derived distance and selection functions (LRE-DM5, LRE-SF1..SF3).
 *
 * <p>When no obstacle is stored under an index, the distance functions return
 * a safe large distance and the obstacle field accessors return zero. The
 * selection functions return index 0 when no obstacle of the requested kind
 * exists (see {@link ObstacleRegister}).
 */
public final class Sensor {

    /** Distance reported for an absent obstacle; far beyond every safety threshold (m). */
    @RoboChartType("real") private static final double SAFE_LARGE_DISTANCE = 1000000.0;

    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;
    private final ObstacleRegister obstacles = new ObstacleRegister();

    /** Updates the AUV's own readings. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** The obstacle register held by the sensor. */
    public ObstacleRegister obstacles() {
        return obstacles;
    }

    /** AUV depth (m below the surface). */
    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity (m/s). */
    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity (m/s). */
    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    /** AUV rate of climb, its vertical velocity (m/s). */
    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at {@code index}: sqrt(ns_rel_dist^2 + ew_rel_dist^2) (m). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = obstacles.lookup(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at {@code index}: |depth - obs_depth| (m). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.abs(depth - obstacles.lookup(index).obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at {@code index}: sqrt(hdist^2 + vdist^2) (m). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DISTANCE;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at {@code index}, or 0 if absent (m). */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at {@code index}, or 0 if absent (m). */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at {@code index}, or 0 if absent (m/s). */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at {@code index}, or 0 if absent (m/s). */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.lookup(index).obs_ew_vel();
    }

    /**
     * Index of the static obstacle with the smallest odist (lowest index on a
     * tie), or 0 if there is no static obstacle.
     */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /**
     * Index of the dynamic obstacle with the smallest odist (lowest index on a
     * tie), or 0 if there is no dynamic obstacle.
     */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = 0.0;
        for (int index : candidates) {
            double distance = odist(index);
            if (closest == ObstacleRegister.NO_OBSTACLE || distance < closestDistance) {
                closest = index;
                closestDistance = distance;
            }
        }
        return closest;
    }
}
