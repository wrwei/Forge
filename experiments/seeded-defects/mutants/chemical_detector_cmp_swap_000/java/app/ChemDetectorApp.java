package chemdetector.app;

import chemdetector.event.EventBus;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.gasanalysis.GasAnalysisController;
import chemdetector.movement.MovementController;
import chemdetector.operation.ChangeDirection;
import chemdetector.vehicle.Clock;
import chemdetector.vehicle.Vehicle;

/**
 * Top-level system wiring. Holds the singleton Vehicle, Clock, EventBus, and
 * both controllers, and exposes a single {@link #step} that fans events out
 * to the gas-analysis and movement subsystems for the current cycle.
 */
public final class ChemDetectorApp {

    private final Vehicle vehicle = new Vehicle();
    private final Clock clock = new Clock();
    private final EventBus bus = new EventBus();
    private final ChangeDirection changeDirection = new ChangeDirection(vehicle);
    private final GasAnalysisController gasAnalysis = new GasAnalysisController(bus);
    private final MovementController movement = new MovementController(vehicle, clock, changeDirection);

    public Vehicle vehicle() { return vehicle; }
    public Clock clock() { return clock; }
    public EventBus bus() { return bus; }
    public GasAnalysisController gasAnalysis() { return gasAnalysis; }
    public MovementController movement() { return movement; }

    /**
     * Single control cycle: tick the clock, deliver one gas-analysis event,
     * then deliver any bus-pending movement event followed by the supplied
     * movement event.
     */
    public void step(GasAnalysisEvent gaEvent, MovementEvent mvEvent) {
        clock.tick();
        gasAnalysis.step(gaEvent);
        MovementEvent crossEvent = bus.poll();
        if (crossEvent == null) {
            movement.step(crossEvent);
        }
        movement.step(mvEvent);
    }
}
