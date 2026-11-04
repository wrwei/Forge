package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, a partial function from natural-number
 * indices to obstacles (LRE-DM3).
 */
public final class ObstacleRegister {

    /**
     * The index that means "no obstacle". It is never a key of the register,
     * so that "none" stays a natural number.
     */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final TreeMap<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Records {@code obstacle} under {@code index}; the index must be at least 1. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle under {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle is recorded under {@code index}. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle under {@code index}; fails if there is none. */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
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
        for (Integer index : obstacles.keySet()) {
            if (obstacles.get(index).isStatic()) {
                result.add(index);
            }
        }
        return result;
    }

    /** Indices of dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer index : obstacles.keySet()) {
            if (!obstacles.get(index).isStatic()) {
                result.add(index);
            }
        }
        return result;
    }
}
