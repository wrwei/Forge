package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import lre.annotation.RoboChartType;

/**
 * Immutable register of obstacles indexed by natural number (LRE-DM3).
 * Supports lookup by index, iteration and static/dynamic filtering.
 */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new ArrayList<Obstacle>();
    }

    public ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = List.copyOf(obstacles);
    }

    /** Copy-on-write addition of one obstacle. */
    public ObstacleRegister with(Obstacle obstacle) {
        List<Obstacle> extended = new ArrayList<Obstacle>(this.obstacles);
        extended.add(obstacle);
        return new ObstacleRegister(extended);
    }

    public List<Obstacle> obstacles() {
        return obstacles;
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return index >= 0 && index < obstacles.size();
    }

    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    public boolean isStatic(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        return obstacle.obsNsVel() == 0.0 && obstacle.obsEwVel() == 0.0;
    }

    public boolean isDynamic(@RoboChartType("nat") int index) {
        Obstacle obstacle = obstacles.get(index);
        return obstacle.obsNsVel() != 0.0 || obstacle.obsEwVel() != 0.0;
    }
}
