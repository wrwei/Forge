package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    private ObstacleRegister(Map<Integer, Obstacle> source) {
        this.obstacles = new HashMap<>(source);
    }

    public ObstacleRegister put(@RoboChartType("nat") int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new HashMap<>(this.obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    public ObstacleRegister remove(@RoboChartType("nat") int index) {
        Map<Integer, Obstacle> copy = new HashMap<>(this.obstacles);
        copy.remove(index);
        return new ObstacleRegister(copy);
    }

    public Optional<Obstacle> get(@RoboChartType("nat") int index) {
        Obstacle o = obstacles.get(index);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of(o);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    public List<Integer> indices() {
        List<Integer> result = new ArrayList<>(obstacles.keySet());
        Collections.sort(result);
        return result;
    }

    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        List<Integer> all = indices();
        for (int i = 0; i < all.size(); i++) {
            int idx = all.get(i);
            Obstacle o = obstacles.get(idx);
            if (o.isStatic()) {
                result.add(idx);
            }
        }
        return result;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        List<Integer> all = indices();
        for (int i = 0; i < all.size(); i++) {
            int idx = all.get(i);
            Obstacle o = obstacles.get(idx);
            if (o.isDynamic()) {
                result.add(idx);
            }
        }
        return result;
    }
}
