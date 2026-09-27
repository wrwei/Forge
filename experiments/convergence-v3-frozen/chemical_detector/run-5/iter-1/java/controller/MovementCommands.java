package chemical_detector.controller;

import java.util.ArrayList;
import java.util.List;

import chemical_detector.domain.Angle;
import chemical_detector.event.InputEvent;

/**
 * The only channel between the two reasoning subsystems: the gas-analysis
 * subsystem emits turn, stop and resume here, and they are delivered, in
 * order, to the movement subsystem.
 */
public final class MovementCommands {

    private final List<InputEvent> pending = new ArrayList<>();

    /** Emits turn carrying direction {@code a}. */
    public void turn(Angle a) {
        pending.add(new InputEvent.Turn(a));
    }

    /** Emits stop. */
    public void stop() {
        pending.add(new InputEvent.Stop());
    }

    /** Emits resume. */
    public void resume() {
        pending.add(new InputEvent.Resume());
    }

    /** Removes and returns the commands emitted since the last drain, oldest first. */
    public List<InputEvent> drain() {
        var emitted = List.copyOf(pending);
        pending.clear();
        return emitted;
    }
}
