package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers, a partial function
 * nat -> Obstacle (LRE-DM3). Identifiers start at 1: index 0 is reserved to
 * mean "no obstacle".
 */
public final class ObstacleRegister {

    /** The index that denotes no obstacle; never a key of the register. */
    @RoboChartType("nat") static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Adds or replaces the obstacle at {@code index} (at least 1). */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1, got " + index);
        }
        obstacles.put(index, Objects.requireNonNull(obstacle, "obstacle"));
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
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** All registered indices, in increasing order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** Indices of the static obstacles, in increasing order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, in increasing order. */
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
