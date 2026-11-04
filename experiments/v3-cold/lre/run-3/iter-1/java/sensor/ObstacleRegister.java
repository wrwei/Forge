package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function from nat
 * to Obstacle. Identifier 0 is reserved as {@link #NO_OBSTACLE}, the index the
 * Sensor reports when no obstacle of the requested kind exists, so the register
 * never holds an obstacle under it.
 */
public final class ObstacleRegister {

    /** The index meaning "no obstacle"; never a key of the register. */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Stores an obstacle under an identifier of at least 1, replacing any previous one. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle at the index, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** True if an obstacle is stored at the index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at the index; the index must be one for which {@link #contains} holds. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new IllegalArgumentException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** Every identifier in the register, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** The identifiers of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isDynamic()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
