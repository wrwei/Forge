package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, as a partial function from natural-number
 * identifiers to obstacles (LRE-DM3).
 *
 * <p>Identifier {@link #NO_OBSTACLE} (0) is reserved: it never names an
 * obstacle, and the sensor's selection functions return it when no obstacle of
 * the requested kind exists. Obstacle identifiers therefore start at 1.
 */
public final class ObstacleRegister {

    /** The reserved index meaning "no obstacle". */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Records (or replaces) the obstacle with the given identifier (at least 1). */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1: " + index);
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

    /** Removes every obstacle. */
    public void clear() {
        obstacles.clear();
    }

    /** Whether an obstacle with the given identifier is registered. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle with the given identifier; it must be registered. */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new IllegalArgumentException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /** Every registered identifier, in increasing order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The identifiers of the static obstacles, in increasing order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstacles.entrySet()) {
            if (item.getValue().isStatic()) {
                result.add(item.getKey());
            }
        }
        return result;
    }

    /** The identifiers of the dynamic obstacles, in increasing order. */
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
