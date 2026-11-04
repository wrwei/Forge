package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// The obstacle register (LRE-DM3): a partial function from natural-number
// indices to obstacles. Index 0 is reserved for "no obstacle", the natural-number
// stand-in for the requirements' -1, so registered indices start at 1.
// Iteration is in ascending index order.
final class ObstacleRegister {

    static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    void put(int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle indices start at 1, got " + index);
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

    // Callers check contains(index) first.
    Obstacle get(int index) {
        return obstacles.get(index);
    }

    List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstacles.entrySet()) {
            if (isStatic(item.getValue())) {
                result.add(item.getKey());
            }
        }
        return result;
    }

    List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> item : obstacles.entrySet()) {
            if (!isStatic(item.getValue())) {
                result.add(item.getKey());
            }
        }
        return result;
    }

    // LRE-DM2: static if both horizontal velocity components are zero.
    static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
