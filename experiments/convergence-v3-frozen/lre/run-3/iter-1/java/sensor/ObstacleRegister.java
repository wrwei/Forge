package lre.sensor;

import java.util.ArrayList;
import java.util.List;

import lre.annotation.RoboChartType;

/**
 * A partial function from natural-number identifiers to obstacles. Lookups
 * outside the registered range are reported as absent rather than failing.
 */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new ArrayList<>();
    }

    private ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** A copy of this register extended with one further obstacle. */
    public ObstacleRegister with(Obstacle obstacle) {
        var extended = new ArrayList<Obstacle>(this.obstacles);
        extended.add(obstacle);
        return new ObstacleRegister(extended);
    }

    /** The number of registered obstacles. */
    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    /** Whether an obstacle is registered at the given index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return index >= 0 && index < obstacles.size();
    }

    /** The obstacle at the given index; the caller must check {@link #contains}. */
    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    /** Whether the obstacle at the given index is registered and static. */
    public boolean isStatic(@RoboChartType("nat") int index) {
        if (!contains(index)) {
            return false;
        }
        return obstacles.get(index).isStatic();
    }

    /** Whether the obstacle at the given index is registered and dynamic. */
    public boolean isDynamic(@RoboChartType("nat") int index) {
        if (!contains(index)) {
            return false;
        }
        return obstacles.get(index).isDynamic();
    }
}
