package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, as a partial function from natural-number
 * identifiers to obstacles (LRE-DM3). Identifier 0 is reserved for
 * {@link Sensor#NO_OBSTACLE} and is never in the register. Iteration is in
 * ascending identifier order.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /**
     * Adds or replaces the obstacle with the given identifier.
     *
     * @throws IllegalArgumentException if {@code index} is not at least 1
     */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 1) {
            throw new IllegalArgumentException("obstacle identifiers start at 1");
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

    /** Whether an obstacle with the given identifier is present. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /**
     * The obstacle with the given identifier.
     *
     * @throws NoSuchElementException if no obstacle has that identifier
     */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle " + index);
        }
        return obstacle;
    }

    /** The identifiers of all obstacles, ascending. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The identifiers of the static obstacles, ascending. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (isStaticObstacle(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** The identifiers of the dynamic obstacles, ascending. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!isStaticObstacle(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private static boolean isStaticObstacle(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
