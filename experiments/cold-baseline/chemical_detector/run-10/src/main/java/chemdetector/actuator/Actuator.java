package chemdetector.actuator;

import chemdetector.event.MovementOutputEvent;
import chemdetector.vehicle.Vehicle;

/**
 * Actuator: receives output events from the movement controller and
 * forwards them to the Vehicle. CD-Evt7 (flag) is the only output.
 */
public final class Actuator {

    private final Vehicle vehicle;
    private MovementOutputEvent lastEvent;

    public Actuator(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.lastEvent = null;
    }

    public void emit(MovementOutputEvent event) {
        this.lastEvent = event;
        if (event instanceof MovementOutputEvent.Flag) {
            vehicle.raiseFlag();
        }
    }

    public MovementOutputEvent lastEvent() {
        return lastEvent;
    }
}
