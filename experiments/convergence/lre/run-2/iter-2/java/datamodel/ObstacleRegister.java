package lre.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable collection of obstacles indexed by natural-number
 * identifiers, modelled as a partial function nat -&gt; Obstacle
 * (LRE-DM3). Supports lookup by index, iteration over all obstacles,
 * and filtering by static/dynamic status.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = Collections.emptyMap();
    }

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = Collections.unmodifiableMap(obstacles);
    }

    /** Copy-on-write insertion of an obstacle at the given index. */
    public ObstacleRegister with(int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<>(obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    /** Copy-on-write removal of the obstacle at the given index. */
    public ObstacleRegister without(int index) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<>(obstacles);
        copy.remove(index);
        return new ObstacleRegister(copy);
    }

    /** Lookup by index; empty when no obstacle is registered there. */
    public Optional<Obstacle> lookup(int index) {
        return Optional.ofNullable(obstacles.get(index));
    }

    /** All registered indices, in insertion order. */
    public List<Integer> indices() {
        List<Integer> out = new ArrayList<>();
        for (Integer index : obstacles.keySet()) {
            out.add(index);
        }
        return out;
    }

    /** Indices of static obstacles only. */
    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    /** Indices of dynamic obstacles only. */
    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (!entry.getValue().isStatic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }
}
