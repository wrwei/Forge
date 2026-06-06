package lre.sensor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lre.annotation.RoboChartType;

/**
 * Immutable register of obstacles indexed by natural-number identifiers,
 * modelled as a partial function nat -&gt; Obstacle (LRE-DM3).
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new LinkedHashMap<>();
    }

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** Returns a copy of this register with the obstacle stored at the index. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<>(obstacles);
        copy.put(Integer.valueOf(index), obstacle);
        return new ObstacleRegister(copy);
    }

    /** Returns a copy of this register without the obstacle at the index. */
    public ObstacleRegister without(@RoboChartType("nat") int index) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<>(obstacles);
        copy.remove(Integer.valueOf(index));
        return new ObstacleRegister(copy);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(Integer.valueOf(index));
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        return Optional.ofNullable(obstacles.get(Integer.valueOf(index)));
    }

    public Set<Integer> indices() {
        return obstacles.keySet();
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }
}
