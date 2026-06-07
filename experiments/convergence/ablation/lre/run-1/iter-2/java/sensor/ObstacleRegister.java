package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Immutable collection of obstacles indexed by natural-number
 * identifiers, modelled as a partial function nat -&gt; Obstacle
 * (LRE-DM3). Supports lookup by index, iteration over all obstacles,
 * and filtering by static/dynamic status.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public static ObstacleRegister empty() {
        return new ObstacleRegister(new TreeMap<>());
    }

    /** Copy-on-write insertion of an obstacle at the given index. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        TreeMap<Integer, Obstacle> copy = new TreeMap<>(obstacles);
        copy.put(Integer.valueOf(index), obstacle);
        return new ObstacleRegister(copy);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(Integer.valueOf(index));
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        Obstacle found = obstacles.get(Integer.valueOf(index));
        if (found == null) {
            return Optional.empty();
        }
        return Optional.of(found);
    }

    /** All registered indices in ascending order. */
    public List<Integer> indices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : obstacles.keySet()) {
            out.add(idx);
        }
        return out;
    }

    /** Indices of static obstacles in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    /** Indices of dynamic obstacles in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isDynamic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }
}
