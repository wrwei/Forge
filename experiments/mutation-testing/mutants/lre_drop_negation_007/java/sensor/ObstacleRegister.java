package lre.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lre.annotation.RoboChartType;

/**
 * Stores obstacles indexed by natural-number identifiers. Models a partial
 * function nat -> Obstacle. Mutable: obstacles can be added or replaced.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<Integer, Obstacle>();
    }

    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        this.obstacles.put(index, obstacle);
    }

    public void clear() {
        this.obstacles.clear();
    }

    public Optional<Obstacle> get(@RoboChartType("nat") int index) {
        Obstacle o = this.obstacles.get(index);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of(o);
    }

    public List<Integer> indices() {
        return new ArrayList<Integer>(this.obstacles.keySet());
    }

    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer idx : this.obstacles.keySet()) {
            Obstacle o = this.obstacles.get(idx);
            if (o.isStatic()) {
                result.add(idx);
            }
        }
        return result;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<Integer>();
        for (Integer idx : this.obstacles.keySet()) {
            Obstacle o = this.obstacles.get(idx);
            if (o.isStatic()) {
                result.add(idx);
            }
        }
        return result;
    }
}
