package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import lre.annotation.RoboChartType;

/**
 * A partial function from natural-number identifiers to obstacles. The
 * identifier of an obstacle is its position in the register. The register is
 * immutable: {@link #add(Obstacle)} returns a new register.
 */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new ArrayList<Obstacle>();
    }

    private ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public ObstacleRegister add(Obstacle obstacle) {
        var copy = new ArrayList<Obstacle>(this.obstacles);
        copy.add(obstacle);
        return new ObstacleRegister(copy);
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
        if (!contains(index)) {
            return false;
        }
        return obstacles.get(index).isStatic();
    }

    public boolean isDynamic(@RoboChartType("nat") int index) {
        if (!contains(index)) {
            return false;
        }
        return obstacles.get(index).isDynamic();
    }
}
