package lre.sensor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Immutable collection of obstacles indexed by natural-number identifiers,
 * modelled as a partial function nat -&gt; Obstacle (LRE-DM3). Mutating
 * operations are copy-on-write and return a new register.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** The register containing no obstacles. */
    public static ObstacleRegister empty() {
        return new ObstacleRegister(new LinkedHashMap<Integer, Obstacle>());
    }

    /** A copy of this register with the obstacle stored at the given index. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<Integer, Obstacle>(obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    /** A copy of this register with the obstacle at the given index removed. */
    public ObstacleRegister without(@RoboChartType("nat") int index) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<Integer, Obstacle>(obstacles);
        copy.remove(index);
        return new ObstacleRegister(copy);
    }

    /** Lookup by index; empty when no obstacle is registered at the index. */
    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        return Optional.ofNullable(obstacle);
    }

    /** All registered indices, in insertion order. */
    public List<Integer> indices() {
        List<Integer> out = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            out.add(entry.getKey());
        }
        return out;
    }

    /** Indices of all static obstacles. */
    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    /** Indices of all dynamic obstacles. */
    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isDynamic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    /** Number of registered obstacles. */
    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }
}
