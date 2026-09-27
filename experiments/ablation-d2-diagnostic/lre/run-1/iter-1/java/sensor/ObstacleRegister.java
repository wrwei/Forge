package lre.sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Obstacles indexed by natural number identifiers (LRE-DM3). The register is
 * immutable: {@link #withObstacle(Obstacle)} returns a fresh copy.
 */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new ArrayList<Obstacle>();
    }

    private ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** Number of obstacles currently registered. */
    public int size() {
        return obstacles.size();
    }

    /** True when {@code index} identifies a registered obstacle. */
    public boolean containsIndex(int index) {
        return index >= 0 && index < obstacles.size();
    }

    /** The obstacle at {@code index}, or an all-zero obstacle if there is none. */
    public Obstacle obstacleAt(int index) {
        if (!containsIndex(index)) {
            return new Obstacle(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        return obstacles.get(index);
    }

    /** True when the obstacle at {@code index} exists and has zero horizontal velocity. */
    public boolean isStaticAt(int index) {
        if (!containsIndex(index)) {
            return false;
        }
        Obstacle obstacle = obstacles.get(index);
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }

    /** True when the obstacle at {@code index} exists and has non-zero horizontal velocity. */
    public boolean isDynamicAt(int index) {
        if (!containsIndex(index)) {
            return false;
        }
        Obstacle obstacle = obstacles.get(index);
        return obstacle.obs_ns_vel() != 0.0 || obstacle.obs_ew_vel() != 0.0;
    }

    /** A copy of this register with {@code obstacle} appended at the next free index. */
    public ObstacleRegister withObstacle(Obstacle obstacle) {
        List<Obstacle> copy = new ArrayList<Obstacle>(obstacles);
        copy.add(obstacle);
        return new ObstacleRegister(copy);
    }
}
