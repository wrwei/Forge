package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * The obstacles known to the AUV, as a partial function from natural-number
 * indices to obstacles.
 */
public final class ObstacleRegister {

    /**
     * The index that denotes "no obstacle". It is a natural number that can
     * never be registered, so every lookup at it falls into the no-obstacle case.
     */
    @RoboChartType("nat")
    static final int NO_OBSTACLE = 2147483647;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<Integer, Obstacle>();

    /**
     * Registers an obstacle at an index, replacing any obstacle already there.
     *
     * @param index    a natural number other than the reserved no-obstacle index
     * @param obstacle the obstacle
     */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 0 || index == NO_OBSTACLE) {
            throw new IllegalArgumentException("index must be a natural number below " + NO_OBSTACLE);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(Integer.valueOf(index), obstacle);
    }

    /**
     * Removes the obstacle at an index, if there is one.
     *
     * @param index the index
     */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(Integer.valueOf(index));
    }

    /**
     * Tells whether an obstacle is registered at an index.
     *
     * @param index the index
     * @return true when the index is in the register's domain
     */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(Integer.valueOf(index));
    }

    /**
     * Looks up the obstacle at an index.
     *
     * @param index an index in the register's domain
     * @return the obstacle at that index
     * @throws NoSuchElementException when no obstacle is registered at the index
     */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(Integer.valueOf(index));
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /**
     * Lists the indices of all registered obstacles, in ascending order.
     *
     * @return the indices
     */
    public List<Integer> indices() {
        return new ArrayList<Integer>(obstacles.keySet());
    }

    /**
     * Lists the indices of the static obstacles, in ascending order.
     *
     * @return the indices of the static obstacles
     */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Lists the indices of the dynamic obstacles, in ascending order.
     *
     * @return the indices of the dynamic obstacles
     */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!isStatic(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Tells whether an obstacle is static: both horizontal velocity components are zero.
     *
     * @param obstacle the obstacle
     * @return true for a static obstacle, false for a dynamic one
     */
    public static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
