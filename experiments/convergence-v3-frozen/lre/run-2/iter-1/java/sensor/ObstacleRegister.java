package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lre.annotation.RoboChartType;

/**
 * The obstacles the AUV currently knows about, indexed by natural number
 * (LRE-DM3). Immutable: {@link #with(Obstacle)} returns a new register.
 */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = Collections.emptyList();
    }

    public ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = List.copyOf(obstacles);
    }

    /** How many obstacles the register holds. */
    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    /** The obstacle at the given index; the index must be in range. */
    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    /** True when the obstacle at the given index has zero horizontal velocity. */
    public boolean isStatic(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        return obstacle.obs_ns_vel() == 0.0 && obstacle.obs_ew_vel() == 0.0;
    }

    /** True when the obstacle at the given index is moving horizontally. */
    public boolean isDynamic(@RoboChartType("nat") int index) {
        return !isStatic(index);
    }

    /** A copy of this register with one more obstacle appended. */
    public ObstacleRegister with(Obstacle obstacle) {
        var extended = new ArrayList<Obstacle>(obstacles);
        extended.add(obstacle);
        return new ObstacleRegister(extended);
    }
}
