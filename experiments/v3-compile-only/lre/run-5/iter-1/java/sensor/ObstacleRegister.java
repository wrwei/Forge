package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function
 * nat -> Obstacle (LRE-DM3). Identifier 0 is reserved to mean "no obstacle",
 * so identifiers in the register start at 1.
 */
public final class ObstacleRegister {

    /** Index meaning "no obstacle"; never a key of the register. */
    static final int NO_OBSTACLE = 0;

    private final TreeMap<Integer, Obstacle> obstacles = new TreeMap<>();

    /**
     * Adds or replaces the obstacle with the given identifier.
     *
     * @param index    identifier, at least 1
     * @param obstacle the obstacle
     */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle identifiers start at 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /**
     * Removes the obstacle with the given identifier, if present.
     *
     * @param index identifier
     */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Removes every obstacle. */
    public void clear() {
        obstacles.clear();
    }

    /**
     * Whether an obstacle has the given identifier.
     *
     * @param index identifier
     * @return true if the identifier is in the register's domain
     */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /**
     * Looks up an obstacle by identifier.
     *
     * @param index identifier in the register's domain
     * @return the obstacle
     * @throws NoSuchElementException if the identifier is not in the domain
     */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /**
     * All identifiers, in increasing order.
     *
     * @return the identifiers
     */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /**
     * Identifiers of the static obstacles (both horizontal velocity
     * components zero), in increasing order.
     *
     * @return the identifiers
     */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer index : obstacles.keySet()) {
            if (isStatic(obstacles.get(index))) {
                result.add(index);
            }
        }
        return result;
    }

    /**
     * Identifiers of the dynamic obstacles (a non-zero horizontal velocity
     * component), in increasing order.
     *
     * @return the identifiers
     */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer index : obstacles.keySet()) {
            if (!isStatic(obstacles.get(index))) {
                result.add(index);
            }
        }
        return result;
    }

    private static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
