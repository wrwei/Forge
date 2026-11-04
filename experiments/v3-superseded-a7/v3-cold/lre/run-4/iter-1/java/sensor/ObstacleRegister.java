package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, a partial function from natural-number
 * identifiers to obstacles (LRE-DM3). Identifier 0 is reserved as
 * {@link #NO_OBSTACLE}, the index the sensor reports when no obstacle of the
 * requested kind exists, so registered identifiers start at 1.
 */
public final class ObstacleRegister {

    /** The index that means "no obstacle" (LRE-Var5, LRE-Var6). */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final TreeMap<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Registers, or replaces, the obstacle with the given identifier (at least 1). */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1");
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle with the given identifier, if any. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle has the given identifier. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle with the given identifier; it must be registered. */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with this identifier");
        }
        return obstacle;
    }

    /** All identifiers, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** The identifiers of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    // LRE-DM2: static when both horizontal velocities are zero.
    private static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
