package lre.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * ObstacleRegister (LRE-DM3): partial function nat -> Obstacle.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> initial) {
        this.obstacles = new HashMap<>(initial);
    }

    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        this.obstacles.put(index, obstacle);
    }

    public void remove(@RoboChartType("nat") int index) {
        this.obstacles.remove(index);
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        return Optional.ofNullable(this.obstacles.get(index));
    }

    public List<Integer> indices() {
        return new ArrayList<>(this.obstacles.keySet());
    }

    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer idx : this.obstacles.keySet()) {
            Obstacle o = this.obstacles.get(idx);
            if (o.isStatic()) {
                result.add(idx);
            }
        }
        return result;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer idx : this.obstacles.keySet()) {
            Obstacle o = this.obstacles.get(idx);
            if (o.isDynamic()) {
                result.add(idx);
            }
        }
        return result;
    }
}
