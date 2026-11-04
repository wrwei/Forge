package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function
 * nat -> Obstacle (LRE-DM3).
 */
public final class ObstacleRegister {

    /** The index that denotes "no obstacle"; real obstacle identifiers are positive. */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    // Sorted by identifier, so iteration (and tie-breaking between equally
    // close obstacles) is deterministic.
    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Adds or replaces the obstacle with the given identifier, which must be positive. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers must be positive: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle with the given identifier, if present. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle with the given identifier exists. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle with the given identifier; the identifier must be present. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /** All identifiers, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** Identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstacles.entrySet()) {
            if (item.getValue().isStatic()) {
                result.add(item.getKey());
            }
        }
        return result;
    }

    /** Identifiers of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstacles.entrySet()) {
            if (!item.getValue().isStatic()) {
                result.add(item.getKey());
            }
        }
        return result;
    }
}
