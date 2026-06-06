package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.clock.Clock;
import chemdetector.event.GAInputEvent;
import chemdetector.event.MVInputEvent;
import chemdetector.event.SharedEvent;
import chemdetector.sensor.Sensor;
import java.util.List;

/**
 * Top-level wiring of the Chemical Detector: two controllers communicating
 * through the shared turn / stop / resume events, plus the Vehicle as the
 * sensing-and-actuation surface (CD-ARCH1, CD-ARCH2).
 */
public final class ChemicalDetectorSystem {

    private final Sensor sensor;
    private final Vehicle vehicle;
    private final Clock clock;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem(Sensor sensor, Vehicle vehicle, Clock clock) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.clock = clock;
        this.gasAnalysis = new GasAnalysisController(sensor);
        this.movement = new MovementController(vehicle, clock);
    }

    public Sensor sensor() {
        return sensor;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public Clock clock() {
        return clock;
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    /** Drives the gas-analysis controller and forwards any emitted shared events to MV. */
    public void stepGA(GAInputEvent event) {
        gasAnalysis.step(event);
        List<SharedEvent> emitted = gasAnalysis.drainOutbox();
        for (int i = 0; i < emitted.size(); i++) {
            SharedEvent se = emitted.get(i);
            if (se instanceof SharedEvent.Turn) {
                SharedEvent.Turn st = (SharedEvent.Turn) se;
                movement.step(new MVInputEvent.Turn(st.a()));
            } else if (se instanceof SharedEvent.Stop) {
                movement.step(new MVInputEvent.Stop());
            } else if (se instanceof SharedEvent.Resume) {
                movement.step(new MVInputEvent.Resume());
            }
        }
    }

    /** Drives the movement controller directly (for Vehicle-side obstacle / odometer events). */
    public void stepMV(MVInputEvent event) {
        movement.step(event);
    }
}
