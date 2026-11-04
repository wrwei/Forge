package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function from nat to Obstacle.
 * Identifier 0 is reserved to mean "no obstacle", so registered identifiers start at 1.
 */
public final class ObstacleRegister {

    /** The index that stands for "no obstacle". */
    @RoboChartType("nat") static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> entries = new TreeMap<>();

    /** Registers (or replaces) the obstacle with the given identifier, which must be at least 1. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1: " + index);
        }
        entries.put(index, Objects.requireNonNull(obstacle));
    }

    /** Whether an obstacle is registered under the given identifier. */
    public boolean contains(@RoboChartType("nat") int index) {
        return entries.containsKey(index);
    }

    /** The obstacle registered under the given identifier; call {@link #contains} first. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = entries.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /** Identifiers of all registered obstacles, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(entries.keySet());
    }

    /** Identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> registered : entries.entrySet()) {
            if (registered.getValue().isStatic()) {
                result.add(registered.getKey());
            }
        }
        return result;
    }

    /** Identifiers of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> registered : entries.entrySet()) {
            if (!registered.getValue().isStatic()) {
                result.add(registered.getKey());
            }
        }
        return result;
    }
}
