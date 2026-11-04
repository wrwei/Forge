package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers, a partial function nat to Obstacle
 * (LRE-DM3). Identifier {@link Sensor#NO_OBSTACLE} (0) is reserved to mean "no obstacle",
 * so stored identifiers start at 1.
 */
public final class ObstacleRegister {

    private final TreeMap<Integer, Obstacle> entries = new TreeMap<>();

    /** Adds or replaces the obstacle with the given identifier (at least 1). */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= Sensor.NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        entries.put(index, obstacle);
    }

    /** Removes the obstacle with the given identifier, if present. */
    public void remove(@RoboChartType("nat") int index) {
        entries.remove(index);
    }

    /** True when the register holds an obstacle with the given identifier. */
    public boolean contains(@RoboChartType("nat") int index) {
        return entries.containsKey(index);
    }

    /** The obstacle with the given identifier; it must be present. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = entries.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /** Every identifier in the register, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(entries.keySet());
    }

    /** The identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : entries.entrySet()) {
            if (entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** The identifiers of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : entries.entrySet()) {
            if (!entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
