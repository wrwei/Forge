package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * LRE-DM3: A register of obstacles keyed by natural number identifiers.
 * Modelled as a partial function nat -> Obstacle.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new LinkedHashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> initial) {
        this.obstacles = new LinkedHashMap<>(initial);
    }

    public ObstacleRegister put(@RoboChartType("nat") int index, Obstacle o) {
        Map<Integer, Obstacle> next = new LinkedHashMap<>(this.obstacles);
        next.put(index, o);
        return new ObstacleRegister(next);
    }

    public ObstacleRegister remove(@RoboChartType("nat") int index) {
        Map<Integer, Obstacle> next = new LinkedHashMap<>(this.obstacles);
        next.remove(index);
        return new ObstacleRegister(next);
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        return Optional.ofNullable(obstacles.get(index));
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    public Map<Integer, Obstacle> asMap() {
        return Collections.unmodifiableMap(obstacles);
    }

    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : obstacles.keySet()) {
            Obstacle o = obstacles.get(idx);
            if (o.isStatic()) {
                out.add(idx);
            }
        }
        return out;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : obstacles.keySet()) {
            Obstacle o = obstacles.get(idx);
            if (o.isDynamic()) {
                out.add(idx);
            }
        }
        return out;
    }
}
