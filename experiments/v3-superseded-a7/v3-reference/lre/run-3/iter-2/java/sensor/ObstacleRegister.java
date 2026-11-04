package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function
 * nat -> Obstacle (LRE-DM3). Iteration is in ascending index order.
 */
public final class ObstacleRegister {

    /**
     * Index reported when no obstacle of the requested kind exists. Indices are
     * natural numbers, so "none" is a value no obstacle may be registered under
     * rather than -1. The value is the largest Java int.
     */
    @RoboChartType("nat") static final int NO_INDEX = 2147483647;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /**
     * Registers (or replaces) the obstacle at {@code index}.
     *
     * @throws IllegalArgumentException if the index is negative or reserved
     */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == NO_INDEX) {
            throw new IllegalArgumentException("obstacle index must be a natural number below " + NO_INDEX);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle at {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle is registered at {@code index}. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /**
     * The obstacle at {@code index}.
     *
     * @throws java.util.NoSuchElementException if none is registered there
     */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new java.util.NoSuchElementException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** All registered indices, ascending. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** Indices of the static obstacles, ascending. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, ascending. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** An obstacle is static if both its horizontal velocities are zero (LRE-DM2). */
    public static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
