package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * The only channel between the two reasoning subsystems: the gas-analysis
 * subsystem posts turn, stop and resume here and the movement subsystem
 * takes them. Holds at most one pending command per control cycle.
 */
public final class MovementBus {

    private MovementEvent pending = new MovementEvent.NoCommand();

    /** Posts a turn command carrying the direction of the strongest signal. */
    public void turn(Angle direction) {
        this.pending = new MovementEvent.Turn(direction);
    }

    /** Posts a stop command: the chemical source has been confirmed. */
    public void stop() {
        this.pending = new MovementEvent.Stop();
    }

    /** Posts a resume command: keep searching. */
    public void resume() {
        this.pending = new MovementEvent.Resume();
    }

    /** Removes and returns the pending command, or {@code NoCommand} if there is none. */
    public MovementEvent take() {
        var command = this.pending;
        this.pending = new MovementEvent.NoCommand();
        return command;
    }
}
