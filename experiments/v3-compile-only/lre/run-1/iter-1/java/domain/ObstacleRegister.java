package lre.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * The obstacle register (LRE-DM3): a partial function from natural-number
 * identifiers to obstacles, with lookup, iteration and static/dynamic
 * filtering. Iteration is in ascending index order.
 */
public final class ObstacleRegister {

    /**
     * The index that denotes "no obstacle". The requirements use -1, but indices
     * are natural numbers, so the largest int is reserved instead; it can never
     * be stored in the register.
     */
    @RoboChartType("nat") public static final int NO_OBSTACLE = Integer.MAX_VALUE;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Stores {@code obstacle} under {@code index}, replacing any previous one. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == NO_OBSTACLE) {
            throw new IllegalArgumentException("index must be a natural number below NO_OBSTACLE");
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle stored under {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether {@code index} is in the domain of the register. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle stored under {@code index}; fails if there is none. */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with index " + index);
        }
        return obstacle;
    }

    /** All indices in the register, ascending. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The indices of the static obstacles, ascending. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** The indices of the dynamic obstacles, ascending. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** An obstacle is static when both obs_ns_vel and obs_ew_vel are zero (LRE-DM2). */
    public static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
