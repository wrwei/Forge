package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function
 * nat -> Obstacle (LRE-DM3).
 *
 * <p>Identifier 0 is reserved: it is the index the selection functions return
 * when no obstacle of the requested kind exists, so no obstacle may be stored
 * under it.
 */
public final class ObstacleRegister {

    /** The index meaning "no obstacle"; never a key of the register. */
    @RoboChartType("nat") static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> entries = new TreeMap<>();

    /**
     * Adds or replaces the obstacle stored under {@code index}.
     *
     * @throws IllegalArgumentException if {@code index} is not a positive identifier
     */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1, got " + index);
        }
        entries.put(index, Objects.requireNonNull(obstacle, "obstacle"));
    }

    /** Removes the obstacle stored under {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        entries.remove(index);
    }

    /** True when an obstacle is stored under {@code index}. */
    public boolean contains(@RoboChartType("nat") int index) {
        return entries.containsKey(index);
    }

    /**
     * The obstacle stored under {@code index}.
     *
     * @throws NoSuchElementException if no obstacle is stored under {@code index}
     */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = entries.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /** All identifiers, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(entries.keySet());
    }

    /** Identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : entries.entrySet()) {
            if (entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Identifiers of the dynamic obstacles, in ascending order. */
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
