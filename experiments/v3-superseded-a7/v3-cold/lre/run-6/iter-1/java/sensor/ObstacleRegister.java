package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, as a partial function from natural-number
 * indices to obstacles. Index 0 is reserved to mean "no obstacle", so stored
 * obstacles use indices from 1.
 */
public final class ObstacleRegister {

    /** The index that names no obstacle. */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> entries = new TreeMap<>();

    /** Stores (or replaces) the obstacle at {@code index}, which must be at least 1. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle indices start at 1");
        }
        entries.put(index, Objects.requireNonNull(obstacle));
    }

    /** Removes the obstacle at {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        entries.remove(index);
    }

    /** Whether an obstacle is stored at {@code index}. */
    public boolean contains(@RoboChartType("nat") int index) {
        return entries.containsKey(index);
    }

    /** The obstacle at {@code index}; the index must be in the register. */
    public Obstacle get(@RoboChartType("nat") int index) {
        Obstacle obstacle = entries.get(index);
        if (obstacle == null) {
            throw new IllegalArgumentException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** Every stored index, in increasing order. */
    public List<Integer> indices() {
        return new ArrayList<>(entries.keySet());
    }

    /** The indices of the static obstacles, in increasing order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : entries.entrySet()) {
            if (isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** The indices of the dynamic obstacles, in increasing order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : entries.entrySet()) {
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
