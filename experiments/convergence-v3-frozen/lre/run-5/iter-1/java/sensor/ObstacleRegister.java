package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import lre.annotation.RoboChartType;

/** Immutable register of obstacles indexed by natural number. */
public record ObstacleRegister(List<Obstacle> obstacles) {

    public static ObstacleRegister empty() {
        return new ObstacleRegister(List.of());
    }

    /** Copy-on-write insertion; the new obstacle takes the next free index. */
    public ObstacleRegister with(Obstacle obstacle) {
        var extended = new ArrayList<Obstacle>(obstacles);
        extended.add(obstacle);
        return new ObstacleRegister(List.copyOf(extended));
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }

    public boolean has(@RoboChartType("nat") int index) {
        return index >= 0 && index < obstacles.size();
    }

    public Obstacle at(@RoboChartType("nat") int index) {
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
        return !obstacles.get(index).isStatic();
    }
}
