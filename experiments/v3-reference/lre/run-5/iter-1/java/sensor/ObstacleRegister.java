package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;

/**
 * Obstacles indexed by natural-number identifiers: a partial function
 * nat -> Obstacle (LRE-DM3). Index {@link LreConstants#NO_OBSTACLE} is
 * reserved to mean "no obstacle" and is never in the domain.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles = new TreeMap<Integer, Obstacle>();

    /** Stores {@code obstacle} under {@code index}, replacing any previous one. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == LreConstants.NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be a natural number other than "
                    + LreConstants.NO_OBSTACLE + ": " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle under {@code index}, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle is stored under {@code index}. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle stored under {@code index}; the index must be present. */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new IllegalArgumentException("no obstacle under index " + index);
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

    // Static: both horizontal velocity components are zero (LRE-DM2).
    private static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
