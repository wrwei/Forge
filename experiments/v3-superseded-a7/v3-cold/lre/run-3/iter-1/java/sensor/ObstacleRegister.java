package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers (LRE-DM3): a partial
 * function from index to {@link Obstacle}. Index {@link #NO_OBSTACLE} is
 * reserved to mean "no obstacle" and is never a key.
 */
public final class ObstacleRegister {

    /** Reserved index meaning "no obstacle"; never a key of the register. */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstaclesByIndex = new TreeMap<>();

    /**
     * Stores the obstacle under the given index, replacing any previous one.
     *
     * @throws IllegalArgumentException if the index is not above
     *         {@link #NO_OBSTACLE} or the obstacle is null
     */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstaclesByIndex.put(index, obstacle);
    }

    /** Removes the obstacle with the given index, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstaclesByIndex.remove(index);
    }

    /** True when an obstacle is stored under the given index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstaclesByIndex.containsKey(index);
    }

    /**
     * The obstacle stored under the given index.
     *
     * @throws NoSuchElementException if no obstacle has that index
     */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstaclesByIndex.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with index " + index);
        }
        return obstacle;
    }

    /** All indices, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstaclesByIndex.keySet());
    }

    /** Indices of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstaclesByIndex.entrySet()) {
            if (item.getValue().isStatic()) {
                result.add(item.getKey());
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstaclesByIndex.entrySet()) {
            if (item.getValue().isDynamic()) {
                result.add(item.getKey());
            }
        }
        return result;
    }
}
