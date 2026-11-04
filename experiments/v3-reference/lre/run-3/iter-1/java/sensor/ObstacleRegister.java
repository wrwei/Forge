package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural numbers (a partial function nat -> Obstacle).
 */
public final class ObstacleRegister {

    /** Largest index that may be stored; the index above it means "no obstacle". */
    @RoboChartType("nat") static final int MAX_INDEX = 2147483646;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<Integer, Obstacle>();

    /** Stores the obstacle at the given index, replacing any previous one. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index > MAX_INDEX) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle at the given index, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle is stored at the given index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at the given index, which must be present. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new IllegalArgumentException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** All indices, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<Integer>(obstacles.keySet());
    }

    /** Indices of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
