package lre.sensor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lre.annotation.RoboChartType;

/**
 * Immutable register of obstacles indexed by natural-number identifiers,
 * modelling a partial function nat -&gt; Obstacle. Copy-on-write updates.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = Collections.emptyMap();
    }

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** Returns a copy of this register with the obstacle stored at index. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new HashMap<>(obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    /** Returns a copy of this register with the obstacle at index removed. */
    public ObstacleRegister without(@RoboChartType("nat") int index) {
        Map<Integer, Obstacle> copy = new HashMap<>(obstacles);
        copy.remove(index);
        return new ObstacleRegister(copy);
    }

    /** True when an obstacle is registered at the given index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /** The obstacle at the given index, or null when absent. */
    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    /** All registered indices. */
    public Set<Integer> indices() {
        return Collections.unmodifiableSet(obstacles.keySet());
    }
}
