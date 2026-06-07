package lre.sensor;

import java.util.List;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * The AUV's environmental sensor interface (LRE-DM5). Provides raw
 * vehicle data (depth, velocities, rate of climb), holds the
 * ObstacleRegister, and computes distances and closest-obstacle
 * selections. When no obstacle exists at a queried index, distance
 * functions return a safe large distance and field accessors return
 * zero.
 */
public final class Sensor {

    /** Safe large distance returned when no obstacle exists. */
    @RoboChartType("real")
    private static final double NO_OBSTACLE_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double depth;
    @RoboChartType("real")
    private double nsVel;
    @RoboChartType("real")
    private double ewVel;
    @RoboChartType("real")
    private double rateOfClimb;
    private ObstacleRegister register = ObstacleRegister.empty();

    /** Updates the raw vehicle data for the current cycle. */
    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    /** Replaces the obstacle register for the current cycle. */
    public void updateRegister(ObstacleRegister register) {
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

    /** Horizontal distance to the obstacle at the given index (LRE-SF1). */
    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            Obstacle obs = found.get();
            return Math.sqrt(obs.nsRelDist() * obs.nsRelDist() + obs.ewRelDist() * obs.ewRelDist());
        }
        return NO_OBSTACLE_DISTANCE;
    }

    /** Vertical distance to the obstacle at the given index (LRE-SF2). */
    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            Obstacle obs = found.get();
            return Math.abs(depth - obs.obsDepth());
        }
        return NO_OBSTACLE_DISTANCE;
    }

    /** Overall Euclidean distance to the obstacle at the given index (LRE-SF3). */
    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (register.contains(index)) {
            return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
        }
        return NO_OBSTACLE_DISTANCE;
    }

    /** North-south relative distance of the obstacle at the given index. */
    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().nsRelDist();
        }
        return 0.0;
    }

    /** East-west relative distance of the obstacle at the given index. */
    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().ewRelDist();
        }
        return 0.0;
    }

    /** North-south velocity of the obstacle at the given index. */
    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().obsNsVel();
        }
        return 0.0;
    }

    /** East-west velocity of the obstacle at the given index. */
    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        Optional<Obstacle> found = register.lookup(index);
        if (found.isPresent()) {
            return found.get().obsEwVel();
        }
        return 0.0;
    }

    /** Index of the nearest static obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestStaticIndex() {
        int best = -1;
        double bestDist = 0.0;
        List<Integer> candidates = register.staticIndices();
        for (Integer idx : candidates) {
            if (best == -1) {
                best = idx.intValue();
                bestDist = odist(idx.intValue());
            } else if (odist(idx.intValue()) < bestDist) {
                best = idx.intValue();
                bestDist = odist(idx.intValue());
            }
        }
        return best;
    }

    /** Index of the nearest dynamic obstacle, or -1 if none exists. */
    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int best = -1;
        double bestDist = 0.0;
        List<Integer> candidates = register.dynamicIndices();
        for (Integer idx : candidates) {
            if (best == -1) {
                best = idx.intValue();
                bestDist = odist(idx.intValue());
            } else if (odist(idx.intValue()) < bestDist) {
                best = idx.intValue();
                bestDist = odist(idx.intValue());
            }
        }
        return best;
    }
}
