package lre.sensor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Immutable collection of obstacles indexed by natural-number
 * identifiers, modelled as a partial function nat -&gt; Obstacle
 * (LRE-DM3). Mutating operations are copy-on-write.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** The empty register. */
    public static ObstacleRegister empty() {
        return new ObstacleRegister(new LinkedHashMap<>());
    }

    /** A copy of this register with the obstacle stored at the given index. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        var copy = new LinkedHashMap<Integer, Obstacle>(obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    /** Whether an obstacle is stored at the given index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at the given index, if any. */
    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        return Optional.ofNullable(obstacles.get(index));
    }

    /** All registered indices. */
    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    /** Indices of all static obstacles. */
    public List<Integer> staticIndices() {
        var result = new ArrayList<Integer>();
        for (Integer index : obstacles.keySet()) {
            if (obstacles.get(index).isStatic()) {
                result.add(index);
            }
        }
        return result;
    }

    /** Indices of all dynamic obstacles. */
    public List<Integer> dynamicIndices() {
        var result = new ArrayList<Integer>();
        for (Integer index : obstacles.keySet()) {
            if (obstacles.get(index).isDynamic()) {
                result.add(index);
            }
        }
        return result;
    }
}
