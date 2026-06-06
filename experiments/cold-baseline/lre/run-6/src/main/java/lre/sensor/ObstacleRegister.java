package lre.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * ObstacleRegister (LRE-DM3): a partial function nat -> Obstacle.
 * Supports lookup by index, iteration over all entries, and filtering
 * by static / dynamic status. Copy-on-write semantics for insertion.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> entries;

    public ObstacleRegister() {
        this.entries = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> initial) {
        this.entries = new HashMap<>(initial);
    }

    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        this.entries.put(index, obstacle);
    }

    public void clear() {
        this.entries.clear();
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        return Optional.ofNullable(entries.get(index));
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return entries.containsKey(index);
    }

    public List<Integer> indices() {
        List<Integer> out = new ArrayList<>(entries.keySet());
        Collections.sort(out);
        return out;
    }

    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : indices()) {
            Obstacle o = entries.get(idx);
            if (o.isStatic()) {
                out.add(idx);
            }
        }
        return out;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : indices()) {
            Obstacle o = entries.get(idx);
            if (o.isDynamic()) {
                out.add(idx);
            }
        }
        return out;
    }

    public int size() {
        return entries.size();
    }
}
