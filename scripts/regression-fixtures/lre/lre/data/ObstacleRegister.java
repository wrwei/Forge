package lre.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lre.annotation.RoboChartType;

public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = new HashMap<>(obstacles);
    }

    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        obstacles.put(index, obstacle);
    }

    public Optional<Obstacle> get(@RoboChartType("nat") int index) {
        return Optional.ofNullable(obstacles.get(index));
    }

    public Map<Integer, Obstacle> all() {
        return Collections.unmodifiableMap(obstacles);
    }

    public Map<Integer, Obstacle> staticObstacles() {
        var result = new HashMap<Integer, Obstacle>();
        for (var entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public Map<Integer, Obstacle> dynamicObstacles() {
        var result = new HashMap<Integer, Obstacle>();
        for (var entry : obstacles.entrySet()) {
            if (entry.getValue().isDynamic()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public int size() {
        return obstacles.size();
    }
}
