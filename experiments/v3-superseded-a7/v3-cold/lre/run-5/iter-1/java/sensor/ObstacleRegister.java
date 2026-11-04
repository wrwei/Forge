package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural number, a partial function nat -> Obstacle (LRE-DM3).
 */
public final class ObstacleRegister {

    /**
     * The index that stands for "no obstacle". It is a natural number that the
     * register never stores, so "none" stays non-negative (Integer.MAX_VALUE).
     */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 2147483647;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Stores {@code obstacle} at {@code index}, replacing any obstacle already there. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == NO_OBSTACLE) {
            throw new IllegalArgumentException("not a storable obstacle index: " + index);
        }
        obstacles.put(index, Objects.requireNonNull(obstacle, "obstacle"));
    }

    /** Removes the obstacle at {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** True when an obstacle is stored at {@code index}. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at {@code index}; the index must be present. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new IllegalArgumentException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** All indices, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** Indices of static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> stored : obstacles.entrySet()) {
            if (stored.getValue().isStatic()) {
                result.add(stored.getKey());
            }
        }
        return result;
    }

    /** Indices of dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> stored : obstacles.entrySet()) {
            if (!stored.getValue().isStatic()) {
                result.add(stored.getKey());
            }
        }
        return result;
    }
}
