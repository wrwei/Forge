package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, as a partial function from natural-number
 * identifiers to obstacles (LRE-DM3). Iteration is in ascending index order.
 */
public final class ObstacleRegister {

    /**
     * Index reserved to mean "no obstacle". Indices are natural numbers, so the
     * requirement's -1 is replaced by an index the register never holds.
     */
    @RoboChartType("nat") static final int NO_OBSTACLE = 2147483647;

    private final TreeMap<Integer, Obstacle> obstacles = new TreeMap<Integer, Obstacle>();

    /** Stores (or replaces) the obstacle at the given index. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == NO_OBSTACLE) {
            throw new IllegalArgumentException("index not available for an obstacle: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle at the given index, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle is stored at the given index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at the given index; the index must be present. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** All stored indices, ascending. */
    public List<Integer> indices() {
        return new ArrayList<Integer>(obstacles.keySet());
    }

    /**
     * Whether the obstacle at the given index is static: both its north-south and
     * east-west velocities are zero (LRE-DM2). The index must be present.
     */
    public boolean isStatic(@RoboChartType("nat") int index) {
        Obstacle obstacle = get(index);
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }

    /** Indices of the static obstacles, ascending. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer index : obstacles.keySet()) {
            if (isStatic(index)) {
                result.add(index);
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, ascending. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer index : obstacles.keySet()) {
            if (!isStatic(index)) {
                result.add(index);
            }
        }
        return result;
    }
}
