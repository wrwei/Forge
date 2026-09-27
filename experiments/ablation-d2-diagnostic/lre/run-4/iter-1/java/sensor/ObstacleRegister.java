package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lre.annotation.RoboChartType;

/**
 * An immutable collection of obstacles indexed by natural number
 * identifiers (LRE-DM3). Index {@code i} denotes the obstacle at
 * position {@code i}; indices outside the register are not in the
 * domain of the register.
 */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = Collections.emptyList();
    }

    public ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = List.copyOf(obstacles);
    }

    /** Copy-on-write extension of the register. */
    public ObstacleRegister withObstacle(Obstacle obstacle) {
        var extended = new ArrayList<Obstacle>(this.obstacles);
        extended.add(obstacle);
        return new ObstacleRegister(extended);
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    /** True when the given index is in the domain of the register. */
    public boolean contains(@RoboChartType("nat") int index) {
        return index >= 0 && index < obstacles.size();
    }

    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    /** An obstacle is static when both horizontal velocity components are zero. */
    public boolean isStatic(@RoboChartType("nat") int index) {
        if (!contains(index)) {
            return false;
        }
        return obstacles.get(index).obsNsVel() == 0.0 && obstacles.get(index).obsEwVel() == 0.0;
    }

    /** An obstacle is dynamic when it is present and not static. */
    public boolean isDynamic(@RoboChartType("nat") int index) {
        if (!contains(index)) {
            return false;
        }
        return !isStatic(index);
    }
}
