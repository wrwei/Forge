package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * LRE-DM3: A partial function nat -> Obstacle, indexed by natural-number ids.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> entries;

    public ObstacleRegister() {
        this.entries = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> entries) {
        this.entries = new HashMap<>(entries);
    }

    public ObstacleRegister put(@RoboChartType("nat") int id, Obstacle obstacle) {
        Map<Integer, Obstacle> next = new HashMap<>(entries);
        next.put(id, obstacle);
        return new ObstacleRegister(next);
    }

    public ObstacleRegister remove(@RoboChartType("nat") int id) {
        Map<Integer, Obstacle> next = new HashMap<>(entries);
        next.remove(id);
        return new ObstacleRegister(next);
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int id) {
        Obstacle o = entries.get(id);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of(o);
    }

    public boolean contains(@RoboChartType("nat") int id) {
        return entries.containsKey(id);
    }

    public List<Integer> ids() {
        List<Integer> result = new ArrayList<>(entries.keySet());
        Collections.sort(result);
        return result;
    }

    public List<Integer> staticIds() {
        List<Integer> result = new ArrayList<>();
        List<Integer> sorted = ids();
        for (int i = 0; i < sorted.size(); i++) {
            int id = sorted.get(i);
            Obstacle o = entries.get(id);
            if (o.isStatic()) {
                result.add(id);
            }
        }
        return result;
    }

    public List<Integer> dynamicIds() {
        List<Integer> result = new ArrayList<>();
        List<Integer> sorted = ids();
        for (int i = 0; i < sorted.size(); i++) {
            int id = sorted.get(i);
            Obstacle o = entries.get(id);
            if (o.isDynamic()) {
                result.add(id);
            }
        }
        return result;
    }

    public int size() {
        return entries.size();
    }
}
