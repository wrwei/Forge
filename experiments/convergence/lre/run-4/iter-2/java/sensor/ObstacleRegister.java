package lre.sensor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable register of obstacles indexed by natural-number identifiers,
 * modelled as a partial function nat -&gt; Obstacle (LRE-DM3).
 * Copy-on-write: {@link #with(int, Obstacle)} returns a new register.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = Collections.unmodifiableMap(new HashMap<Integer, Obstacle>());
    }

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = Collections.unmodifiableMap(obstacles);
    }

    /** Returns a new register with the obstacle stored at the given index. */
    public ObstacleRegister with(int index, Obstacle obstacle) {
        var copy = new HashMap<Integer, Obstacle>(this.obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    /** Looks up the obstacle at the given index. */
    public Optional<Obstacle> lookup(int index) {
        return Optional.ofNullable(obstacles.get(index));
    }

    /** True when an obstacle is registered at the given index. */
    public boolean contains(int index) {
        return obstacles.containsKey(index);
    }

    /** All registered indices. */
    public Set<Integer> indices() {
        return obstacles.keySet();
    }
}
