package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import lre.annotation.RoboChartType;

/** Obstacles indexed by natural-number identifier. Copy-on-write. */
public final class ObstacleRegister {

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new ArrayList<Obstacle>();
    }

    private ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** Returns a new register with the obstacle appended at the next index. */
    public ObstacleRegister with(Obstacle obstacle) {
        var next = new ArrayList<Obstacle>(obstacles);
        next.add(obstacle);
        return new ObstacleRegister(next);
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    /** True when the partial function is defined at the given index. */
    public boolean has(@RoboChartType("nat") int index) {
        return index >= 0 && index < obstacles.size();
    }

    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    public boolean isStatic(@RoboChartType("nat") int index) {
        if (!has(index)) {
            return false;
        }
        return obstacles.get(index).isStatic();
    }

    public boolean isDynamic(@RoboChartType("nat") int index) {
        if (!has(index)) {
            return false;
        }
        return obstacles.get(index).isDynamic();
    }
}
