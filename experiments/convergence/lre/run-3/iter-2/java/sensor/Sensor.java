package lre.sensor;

import java.util.Optional;
import lre.annotation.RoboChartType;

/**
 * The AUV sensor interface (LRE-DM5). Provides raw environmental data,
 * obstacle distance functions, obstacle field accessors, and closest-obstacle
 * selection. When no obstacle exists at an index, distance functions return a
 * safe large distance and field accessors return zero.
 */
public final class Sensor {

    /** Safe large distance returned when no obstacle exists. */
    @RoboChartType("real")
    private static final double NO_OBSTACLE_DIST = 1000.0;

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
    public void update(@RoboChartType("real") double depth, @RoboChartType("real") double nsVel,
            @RoboChartType("real") double ewVel, @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Replaces the obstacle register. */
    public void setRegister(ObstacleRegister register) {
        this.register = register;
    }

    public ObstacleRegister register() {
        return register;
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

    /** Horizontal distance to the obstacle at index (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            Obstacle obs = found.get();
            return Math.sqrt(obs.nsRelDist() * obs.nsRelDist() + obs.ewRelDist() * obs.ewRelDist());
        }
        return NO_OBSTACLE_DIST;
    }

    /** Vertical distance to the obstacle at index (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            Obstacle obs = found.get();
            return Math.abs(depth - obs.obsDepth());
        }
        return NO_OBSTACLE_DIST;
    }

    /** Overall Euclidean distance to the obstacle at index (LRE-SF3). */
    @RoboChartType("real")
    public double odist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
        }
        return NO_OBSTACLE_DIST;
    }

    /** North-south relative distance of the obstacle at index, or 0 if absent. */
    @RoboChartType("real")
    public double nsRelDist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().nsRelDist();
        }
        return 0.0;
    }

    /** East-west relative distance of the obstacle at index, or 0 if absent. */
    @RoboChartType("real")
    public double ewRelDist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().ewRelDist();
        }
        return 0.0;
    }

    /** North-south velocity of the obstacle at index, or 0 if absent. */
    @RoboChartType("real")
    public double obsNsVel(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().obsNsVel();
        }
        return 0.0;
    }

    /** East-west velocity of the obstacle at index, or 0 if absent. */
    @RoboChartType("real")
    public double obsEwVel(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().obsEwVel();
        }
        return 0.0;
    }

    /**
     * Index of the closest static obstacle by overall distance, or -1 if no
     * static obstacle exists.
     */
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = 0.0;
        for (Integer index : register.indices()) {
            Optional<Obstacle> found = register.lookup(index);
            if (found.isPresent() && found.get().isStatic()) {
                if (best == -1 || odist(index) < bestDist) {
                    best = index;
                    bestDist = odist(index);
                }
            }
        }
        return best;
    }

    /**
     * Index of the closest dynamic obstacle by overall distance, or -1 if no
     * dynamic obstacle exists.
     */
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = 0.0;
        for (Integer index : register.indices()) {
            Optional<Obstacle> found = register.lookup(index);
            if (found.isPresent() && found.get().isDynamic()) {
                if (best == -1 || odist(index) < bestDist) {
                    best = index;
                    bestDist = odist(index);
                }
            }
        }
        return best;
    }

    /**
     * Time at Closest Point of Approach to the obstacle at index, using the
     * obstacle's horizontal relative position and velocity. Returns -1 when no
     * obstacle exists at the index or the obstacle has zero horizontal
     * velocity (no approach to compute).
     */
    @RoboChartType("real")
    public double cpaTime(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            Obstacle obs = found.get();
            double speedSq = obs.obsNsVel() * obs.obsNsVel() + obs.obsEwVel() * obs.obsEwVel();
            if (speedSq > 0.0) {
                return -(obs.nsRelDist() * obs.obsNsVel() + obs.ewRelDist() * obs.obsEwVel()) / speedSq;
            }
        }
        return -1.0;
    }

    /**
     * Closest Distance of Approach to the obstacle at index, using the
     * obstacle's horizontal relative position and velocity. Returns the
     * current overall distance when the obstacle has zero horizontal velocity,
     * and a safe large distance when no obstacle exists at the index.
     */
    @RoboChartType("real")
    public double cpaDist(int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            Obstacle obs = found.get();
            double speedSq = obs.obsNsVel() * obs.obsNsVel() + obs.obsEwVel() * obs.obsEwVel();
            if (speedSq > 0.0) {
                double t = cpaTime(index);
                double nsAtCpa = obs.nsRelDist() + obs.obsNsVel() * t;
                double ewAtCpa = obs.ewRelDist() + obs.obsEwVel() * t;
                return Math.sqrt(nsAtCpa * nsAtCpa + ewAtCpa * ewAtCpa);
            }
            return odist(index);
        }
        return NO_OBSTACLE_DIST;
    }
}
