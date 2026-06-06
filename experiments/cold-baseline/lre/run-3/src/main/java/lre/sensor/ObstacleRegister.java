package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lre.annotation.RoboChartType;

/**
 * LRE-DM3: Partial function nat -> Obstacle.
 * Indexed by non-negative integer identifiers.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = new HashMap<>(obstacles);
    }

    public ObstacleRegister put(@RoboChartType("nat") int index, Obstacle o) {
        Map<Integer, Obstacle> next = new HashMap<>(this.obstacles);
        next.put(index, o);
        return new ObstacleRegister(next);
    }

    public ObstacleRegister remove(@RoboChartType("nat") int index) {
        Map<Integer, Obstacle> next = new HashMap<>(this.obstacles);
        next.remove(index);
        return new ObstacleRegister(next);
    }

    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    public List<Integer> indices() {
        List<Integer> idx = new ArrayList<>(obstacles.keySet());
        Collections.sort(idx);
        return idx;
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
