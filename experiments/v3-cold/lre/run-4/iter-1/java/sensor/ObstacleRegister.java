package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function from
 * nat to {@link Obstacle} (LRE-DM3). Identifiers start at 1; index
 * {@link #NO_OBSTACLE} is never mapped and stands for "no obstacle".
 */
public final class ObstacleRegister {

    /** Index that never holds an obstacle; used when no obstacle exists. */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Maps {@code index} (at least 1) to {@code obstacle}, replacing any previous entry. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
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

    /** The obstacle at {@code index}; the index must be registered. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
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

    // Static: both horizontal velocity components are zero (LRE-DM2).
    private static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
