package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import lre.annotation.RoboChartType;

/** Obstacles indexed by natural number, as a partial function nat -> Obstacle. */
public final class ObstacleRegister {

    private static final Obstacle ABSENT = new Obstacle(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

    private final List<Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new ArrayList<Obstacle>();
    }

    public ObstacleRegister(List<Obstacle> obstacles) {
        this.obstacles = new ArrayList<Obstacle>(obstacles);
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    public boolean has(@RoboChartType("nat") int index) {
        return index >= 0 && index < obstacles.size();
    }

    public Obstacle at(@RoboChartType("nat") int index) {
        if (!has(index)) {
            return ABSENT;
        }
        return obstacles.get(index);
    }

    public boolean isStaticAt(@RoboChartType("nat") int index) {
        return has(index) && obstacles.get(index).isStatic();
    }

    public boolean isDynamicAt(@RoboChartType("nat") int index) {
        return has(index) && !obstacles.get(index).isStatic();
    }

    /** Copy-on-write append. */
    public ObstacleRegister with(Obstacle obstacle) {
        List<Obstacle> next = new ArrayList<Obstacle>(obstacles);
        next.add(obstacle);
        return new ObstacleRegister(next);
    }
}
