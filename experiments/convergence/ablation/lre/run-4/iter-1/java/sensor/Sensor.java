package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental sensor (LRE-DM5). Provides raw kinematic data,
 * the obstacle register, distance functions, obstacle field accessors,
 * closest-obstacle selection, and closest-point-of-approach computations.
 *
 * <p>Missing-data policy: when no obstacle exists at the requested index,
 * distance functions return a safe large distance and field accessors
 * return zero (LRE-DM5).
 */
public final class Sensor {

    private static final double SAFE_LARGE_DISTANCE = 1.0e9;

    private double depth;
    private double nsVel;
    private double ewVel;
    private double rateOfClimb;
    private ObstacleRegister obstacles = ObstacleRegister.empty();

    /** Replaces the sensor's current readings with fresh data. */
    public void update(double newDepth, double newNsVel, double newEwVel,
                       double newRateOfClimb, ObstacleRegister newObstacles) {
        this.depth = newDepth;
        this.nsVel = newNsVel;
        this.ewVel = newEwVel;
        this.rateOfClimb = newRateOfClimb;
        this.obstacles = newObstacles;
    }

    /** AUV depth below the surface (metres). */
    public double depth() {
        return depth;
    }

    /** AUV north-south horizontal velocity (m/s). */
    public double nsVel() {
        return nsVel;
    }

    /** AUV east-west horizontal velocity (m/s). */
    public double ewVel() {
        return ewVel;
    }

    /** AUV vertical velocity (m/s). */
    public double rateOfClimb() {
        return rateOfClimb;
    }

    /** The current obstacle register. */
    public ObstacleRegister obstacles() {
        return obstacles;
    }

    /**
     * Horizontal distance to the obstacle at the given index (LRE-SF1):
     * sqrt(ns_rel_dist^2 + ew_rel_dist^2).
     */
    public double hdist(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = entry.get();
        return Math.sqrt(obstacle.nsRelDist() * obstacle.nsRelDist()
                + obstacle.ewRelDist() * obstacle.ewRelDist());
    }

    /**
     * Vertical distance to the obstacle at the given index (LRE-SF2):
     * |depth - obs_depth|.
     */
    public double vdist(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = entry.get();
        return Math.abs(depth - obstacle.obsDepth());
    }

    /**
     * Overall Euclidean distance to the obstacle at the given index
     * (LRE-SF3): sqrt(hdist^2 + vdist^2).
     */
    public double odist(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at the index; 0 if absent. */
    public double nsRelDist(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return 0.0;
        }
        return entry.get().nsRelDist();
    }

    /** East-west relative distance of the obstacle at the index; 0 if absent. */
    public double ewRelDist(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return 0.0;
        }
        return entry.get().ewRelDist();
    }

    /** North-south velocity of the obstacle at the index; 0 if absent. */
    public double obsNsVel(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return 0.0;
        }
        return entry.get().obsNsVel();
    }

    /** East-west velocity of the obstacle at the index; 0 if absent. */
    public double obsEwVel(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return 0.0;
        }
        return entry.get().obsEwVel();
    }

    /**
     * Index of the static obstacle with minimal overall distance, or -1
     * when no static obstacle exists.
     */
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = 0.0;
        List<Integer> candidates = obstacles.staticIndices();
        for (int index : candidates) {
            double dist = odist(index);
            if (best == -1 || dist < bestDist) {
                best = index;
                bestDist = dist;
            }
        }
        return best;
    }

    /**
     * Index of the dynamic obstacle with minimal overall distance, or -1
     * when no dynamic obstacle exists.
     */
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = 0.0;
        List<Integer> candidates = obstacles.dynamicIndices();
        for (int index : candidates) {
            double dist = odist(index);
            if (best == -1 || dist < bestDist) {
                best = index;
                bestDist = dist;
            }
        }
        return best;
    }

    /**
     * Time at the closest point of approach (seconds) to the obstacle at
     * the given index, computed from the obstacle's position and velocity
     * relative to the AUV. Returns 0 when no obstacle exists at the index
     * or when the relative velocity is zero.
     */
    public double cpaTime(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return 0.0;
        }
        Obstacle obstacle = entry.get();
        double relVelNs = obstacle.obsNsVel() - nsVel;
        double relVelEw = obstacle.obsEwVel() - ewVel;
        double relSpeedSq = relVelNs * relVelNs + relVelEw * relVelEw;
        if (relSpeedSq == 0.0) {
            return 0.0;
        }
        return -(obstacle.nsRelDist() * relVelNs + obstacle.ewRelDist() * relVelEw)
                / relSpeedSq;
    }

    /**
     * Closest distance of approach (metres) to the obstacle at the given
     * index. Evaluated at the closest point of approach, clamped to now
     * when the closest point lies in the past. Returns a safe large
     * distance when no obstacle exists at the index.
     */
    public double cpaDist(@RoboChartType("nat") int index) {
        Optional<Obstacle> entry = obstacles.lookup(index);
        if (entry.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle obstacle = entry.get();
        double atTime = cpaTime(index);
        if (atTime < 0.0) {
            atTime = 0.0;
        }
        double relVelNs = obstacle.obsNsVel() - nsVel;
        double relVelEw = obstacle.obsEwVel() - ewVel;
        double nsAtCpa = obstacle.nsRelDist() + relVelNs * atTime;
        double ewAtCpa = obstacle.ewRelDist() + relVelEw * atTime;
        return Math.sqrt(nsAtCpa * nsAtCpa + ewAtCpa * ewAtCpa);
    }
}
