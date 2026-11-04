package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural-number identifiers: a partial function
 * nat -> Obstacle. Identifiers start at 1; {@link #NO_OBSTACLE} is reserved
 * to mean "no obstacle" and is never stored.
 */
public final class ObstacleRegister {

    /**
     * Index meaning "no obstacle". The requirements use -1, which a natural
     * number cannot hold, so the register reserves 0 instead.
     */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final Map<Integer, Obstacle> obstacles = new TreeMap<>();

    /** Stores (or replaces) the obstacle with the given identifier, which must be at least 1. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index < 1) {
            throw new IllegalArgumentException("obstacle identifiers start at 1; 0 means no obstacle");
        }
        obstacles.put(index, obstacle);
    }

    /** Removes the obstacle with the given identifier, if present. */
    public void remove(@RoboChartType("nat") int index) {
        obstacles.remove(index);
    }

    /** Whether an obstacle with the given identifier is stored. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle with the given identifier; it must be stored. */
    public Obstacle lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        if (obstacle == null) {
            throw new NoSuchElementException("no obstacle with identifier " + index);
        }
        return obstacle;
    }

    /** Whether the stored obstacle with the given identifier is static (both horizontal velocities zero). */
    public boolean isStatic(@RoboChartType("nat") int index) {
        Obstacle obstacle = lookup(index);
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }

    /** The identifiers of all stored obstacles, in ascending order. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** The identifiers of the static obstacles, in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (int index : obstacles.keySet()) {
            if (isStatic(index)) {
                result.add(index);
            }
        }
        return result;
    }

    /** The identifiers of the dynamic obstacles, in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (int index : obstacles.keySet()) {
            if (!isStatic(index)) {
                result.add(index);
            }
        }
        return result;
    }
}
