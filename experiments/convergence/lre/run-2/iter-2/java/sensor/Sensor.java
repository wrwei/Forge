package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;
import lre.datamodel.Obstacle;
import lre.datamodel.ObstacleRegister;

/**
 * The AUV's environmental sensor interface (LRE-DM5). Provides raw
 * vehicle data (depth, velocities), distance functions to registered
 * obstacles, obstacle field accessors, and closest-obstacle selection.
 *
 * <p>When no obstacle exists at the requested index, distance functions
 * return a safe large distance and field accessors return zero, so the
 * operation layer needs no sentinel checks.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double SAFE_LARGE_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private ObstacleRegister register = new ObstacleRegister();

    /** Update the raw vehicle data for the current control cycle. */
    public void updateVehicle(
            @RoboChartType("real") double depth,
            @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel,
            @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Replace the obstacle register for the current control cycle. */
    public void updateObstacles(ObstacleRegister register) {
        this.register = register;
    }

    /** AUV depth, metres below surface. */
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

    /**
     * Horizontal distance to the obstacle at the given index (LRE-SF1):
     * sqrt(nsRelDist^2 + ewRelDist^2). Safe large distance when no
     * obstacle exists at the index.
     */
    public double hdist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle o = found.get();
        return Math.sqrt(o.nsRelDist() * o.nsRelDist() + o.ewRelDist() * o.ewRelDist());
    }

    /**
     * Vertical distance to the obstacle at the given index (LRE-SF2):
     * |depth - obsDepth|. Safe large distance when no obstacle exists
     * at the index.
     */
    public double vdist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        Obstacle o = found.get();
        return Math.abs(depth - o.obsDepth());
    }

    /**
     * Overall Euclidean distance to the obstacle at the given index
     * (LRE-SF3): sqrt(hdist^2 + vdist^2). Safe large distance when no
     * obstacle exists at the index.
     */
    public double odist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return SAFE_LARGE_DISTANCE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    /** North-south relative distance of the obstacle at index; zero when missing. */
    public double nsRelDist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().nsRelDist();
    }

    /** East-west relative distance of the obstacle at index; zero when missing. */
    public double ewRelDist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().ewRelDist();
    }

    /** North-south velocity of the obstacle at index; zero when missing. */
    public double obsNsVel(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().obsNsVel();
    }

    /** East-west velocity of the obstacle at index; zero when missing. */
    public double obsEwVel(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isEmpty()) {
            return 0.0;
        }
        return found.get().obsEwVel();
    }

    /** Index of the nearest static obstacle by overall distance, or -1 if none. */
    public int closestStaticIndex() {
        List<Integer> candidates = register.staticIndices();
        int best = -1;
        double bestDist = 0.0;
        for (int i = 0; i < candidates.size(); i++) {
            int index = candidates.get(i);
            double d = odist(index);
            if (best == -1 || d < bestDist) {
                best = index;
                bestDist = d;
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle by overall distance, or -1 if none. */
    public int closestDynamicIndex() {
        List<Integer> candidates = register.dynamicIndices();
        int best = -1;
        double bestDist = 0.0;
        for (int i = 0; i < candidates.size(); i++) {
            int index = candidates.get(i);
            double d = odist(index);
            if (best == -1 || d < bestDist) {
                best = index;
                bestDist = d;
            }
        }
        return best;
    }
}
