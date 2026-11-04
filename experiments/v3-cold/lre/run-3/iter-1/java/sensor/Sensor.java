package lre.sensor;

import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The AUV's raw environmental data and its obstacle register, with the
 * distance, field-accessor and selection functions the LRE reads each step.
 * An index with no obstacle (in particular {@link ObstacleRegister#NO_OBSTACLE})
 * has a safe large distance and zero relative position and velocity.
 */
public final class Sensor {

    /** Distance reported for an index with no obstacle (m); far beyond every threshold. */
    @RoboChartType("real") private static final double SAFE_LARGE_DIST = 1000000.0;

    private final ObstacleRegister obstacles;
    @RoboChartType("real") private double depth;
    @RoboChartType("real") private double nsVel;
    @RoboChartType("real") private double ewVel;
    @RoboChartType("real") private double rateOfClimb;

    /** A sensor reading the given register; the raw data start at zero. */
    public Sensor(ObstacleRegister obstacles) {
        this.obstacles = obstacles;
    }

    /** Records a new reading of the AUV's depth (m) and velocity components (m/s). */
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** AUV depth below the surface (m). */
    @RoboChartType("real") public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity (m/s). */
    @RoboChartType("real") public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity (m/s). */
    @RoboChartType("real") public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity (m/s). */
    @RoboChartType("real") public double rateOfClimb() {
        return rateOfClimb;
    }

    /** Horizontal distance to the obstacle at the index (m). */
    @RoboChartType("real") public double hdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = obstacles.get(index);
        return Math.sqrt(obstacle.ns_rel_dist() * obstacle.ns_rel_dist()
                + obstacle.ew_rel_dist() * obstacle.ew_rel_dist());
    }

    /** Vertical distance to the obstacle at the index (m). */
    @RoboChartType("real") public double vdist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        Obstacle obstacle = obstacles.get(index);
        return Math.abs(depth - obstacle.obs_depth());
    }

    /** Overall Euclidean distance to the obstacle at the index (m). */
    @RoboChartType("real") public double odist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return SAFE_LARGE_DIST;
        }
        double horizontal = hdist(index);
        double vertical = vdist(index);
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    /** North-south relative distance of the obstacle at the index (m). */
    @RoboChartType("real") public double nsRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).ns_rel_dist();
    }

    /** East-west relative distance of the obstacle at the index (m). */
    @RoboChartType("real") public double ewRelDist(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).ew_rel_dist();
    }

    /** North-south velocity of the obstacle at the index (m/s). */
    @RoboChartType("real") public double obsNsVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).obs_ns_vel();
    }

    /** East-west velocity of the obstacle at the index (m/s). */
    @RoboChartType("real") public double obsEwVel(@RoboChartType("nat") int index) {
        if (!obstacles.contains(index)) {
            return 0.0;
        }
        return obstacles.get(index).obs_ew_vel();
    }

    /** Index of the static obstacle with the least overall distance, or NO_OBSTACLE if there is none. */
    @RoboChartType("nat") public int closestStaticIndex() {
        return closestOf(obstacles.staticIndices());
    }

    /** Index of the dynamic obstacle with the least overall distance, or NO_OBSTACLE if there is none. */
    @RoboChartType("nat") public int closestDynamicIndex() {
        return closestOf(obstacles.dynamicIndices());
    }

    // Ties go to the lowest index, since the candidates are in ascending order.
    private int closestOf(List<Integer> candidates) {
        int closest = ObstacleRegister.NO_OBSTACLE;
        double closestDistance = SAFE_LARGE_DIST;
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
