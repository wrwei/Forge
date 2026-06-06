package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lre.annotation.RoboChartType;

public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new LinkedHashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> initial) {
        this.obstacles = new LinkedHashMap<>(initial);
    }

    public ObstacleRegister put(@RoboChartType("nat") int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<>(this.obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    public List<Integer> indices() {
        return Collections.unmodifiableList(new ArrayList<>(obstacles.keySet()));
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
