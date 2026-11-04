package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/** Obstacles indexed by natural numbers: a partial function from nat to Obstacle (LRE-DM3). */
public final class ObstacleRegister {

    // Reported by the sensor's selection functions when no obstacle qualifies; never a key.
    @RoboChartType("nat") static final int NO_INDEX = Integer.MAX_VALUE;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Adds or replaces the obstacle at {@code index}. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == NO_INDEX) {
            throw new IllegalArgumentException("obstacle index out of range: " + index);
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

    /** True when an obstacle is registered at {@code index}. */
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
            if (entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, ascending. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
