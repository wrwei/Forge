package lre.data;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * The obstacle register (LRE-DM3): a partial function from natural-number
 * indices to obstacles. Index {@link #NO_INDEX} (zero) is reserved to mean
 * "no obstacle", so obstacles are stored at indices of one or more.
 */
public final class ObstacleRegister {

    /** The index that denotes "no obstacle"; never a key of the register. */
    public static final int NO_INDEX = 0;

    private final TreeMap<Integer, Obstacle> obstacles = new TreeMap<Integer, Obstacle>();

    /**
     * Stores (or replaces) the obstacle at the given index.
     *
     * @param index    the obstacle's identifier, at least one
     * @param obstacle the obstacle
     */
    public void put(int index, Obstacle obstacle) {
        if (index <= NO_INDEX) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
        }
        if (obstacle == null) {
            throw new IllegalArgumentException("obstacle must not be null");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle at the given index, if any. */
    public void remove(int index) {
        obstacles.remove(index);
    }

    /** Removes every obstacle. */
    public void clear() {
        obstacles.clear();
    }

    /** Whether an obstacle is stored at the given index. */
    public boolean contains(int index) {
        return obstacles.containsKey(index);
    }

    /**
     * Looks up the obstacle at the given index.
     *
     * @throws NoSuchElementException if no obstacle is stored there
     */
    public Obstacle lookup(int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle at index " + index);
        }
        return obstacle;
    }

    /** The number of obstacles stored. */
    public int size() {
        return obstacles.size();
    }

    /** Every index in the register, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<Integer>(obstacles.keySet());
    }

    /** The indices of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer index : obstacles.keySet()) {
            if (isStatic(obstacles.get(index))) {
                result.add(index);
            }
        }
        return result;
    }

    /** The indices of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer index : obstacles.keySet()) {
            if (!isStatic(obstacles.get(index))) {
                result.add(index);
            }
        }
        return result;
    }

    /** Whether an obstacle is static: both horizontal velocity components are zero. */
    public static boolean isStatic(Obstacle obstacle) {
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }
}
