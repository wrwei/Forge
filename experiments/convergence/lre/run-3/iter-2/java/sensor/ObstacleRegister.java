package lre.sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Stores obstacles indexed by natural-number identifiers, modelled as a
 * partial function nat -&gt; Obstacle (LRE-DM3). Copy-on-write immutable.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** Returns a new register with the obstacle stored at the given index. */
    public ObstacleRegister with(int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new HashMap<>(this.obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    /** Returns a new register with the obstacle at the given index removed. */
    public ObstacleRegister without(int index) {
        Map<Integer, Obstacle> copy = new HashMap<>(this.obstacles);
        copy.remove(index);
        return new ObstacleRegister(copy);
    }

    /** Looks up the obstacle at the given index, if present. */
    public Optional<Obstacle> lookup(int index) {
        return Optional.ofNullable(obstacles.get(index));
    }

    /** Returns true when an obstacle exists at the given index. */
    public boolean contains(int index) {
        return obstacles.containsKey(index);
    }

    /** All indices currently registered. */
    public Set<Integer> indices() {
        return obstacles.keySet();
    }

    /** Number of registered obstacles. */
    public int size() {
        return obstacles.size();
    }
}
