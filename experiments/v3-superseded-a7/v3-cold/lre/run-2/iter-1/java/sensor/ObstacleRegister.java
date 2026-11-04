package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural numbers: a partial function nat -> Obstacle (LRE-DM3).
 * Index 0 is reserved to mean "no obstacle", so obstacles are numbered from 1.
 */
public final class ObstacleRegister {

    /** The index that denotes "no obstacle"; never a key of the register. */
    @RoboChartType("nat") static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Records the obstacle at an index (1 or more), replacing any obstacle already there. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle at an index, if there is one. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** True when there is an obstacle at the index. */
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

    /** The indices of all obstacles, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The indices of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> slot : obstacles.entrySet()) {
            if (slot.getValue().isStatic()) {
                result.add(slot.getKey());
            }
        }
        return result;
    }

    /** The indices of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> slot : obstacles.entrySet()) {
            if (!slot.getValue().isStatic()) {
                result.add(slot.getKey());
            }
        }
        return result;
    }
}
