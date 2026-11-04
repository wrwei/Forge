package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.controller.LreConstants;

/**
 * The obstacles known to the sensor, as a partial function from natural-number
 * indices to obstacles (LRE-DM3). Index {@link LreConstants#NO_OBSTACLE} is
 * reserved for "no obstacle" and is never in the domain. Iteration is in
 * ascending index order.
 */
final class ObstacleRegister {

    private final TreeMap<Integer, Obstacle> obstacles = new TreeMap<>();

    void put(int index, Obstacle obstacle) {
        if (index <= LreConstants.NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    void remove(int index) {
        obstacles.remove(index);
    }

    boolean contains(int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at {@code index}; the index must be in the domain. */
    Obstacle lookup(int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** All indices in the domain, ascending. */
    List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** Indices of the static obstacles, ascending. */
    List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> mapping : obstacles.entrySet()) {
            if (isStatic(mapping.getValue())) {
                result.add(mapping.getKey());
            }
        }
        return result;
    }

    /** Indices of the dynamic obstacles, ascending. */
    List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> mapping : obstacles.entrySet()) {
            if (!isStatic(mapping.getValue())) {
                result.add(mapping.getKey());
            }
        }
        return result;
    }

    /** Static means both horizontal velocity components are zero (LRE-DM2). */
    static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
