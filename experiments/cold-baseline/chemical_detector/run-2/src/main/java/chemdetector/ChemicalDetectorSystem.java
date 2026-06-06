package chemdetector;

import chemdetector.clock.StuckClock;
import chemdetector.gasanalysis.GasAnalysisController;
import chemdetector.gasanalysis.event.OutputEvent;
import chemdetector.movement.MovementController;
import chemdetector.movement.event.InputEvent;
import chemdetector.sensor.GasFunctions;
import chemdetector.vehicle.Vehicle;
import java.util.List;

/**
 * Top-level wiring for the Chemical Detector (CD-ARCH1, CD-ARCH2).
 *
 * <p>The constructor instantiates the three subsystems described in
 * CD-ARCH2: the Vehicle, the gas-analysis controller, and the
 * movement controller. The {@link #route} method copies events
 * emitted by the gas-analysis subsystem (turn / stop / resume) into
 * the movement subsystem's input queue.
 */
public final class ChemicalDetectorSystem {

    private final Vehicle vehicle;
    private final GasFunctions gasFunctions;
    private final StuckClock clock;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem() {
        this.vehicle = new Vehicle();
        this.gasFunctions = new GasFunctions();
        this.clock = new StuckClock();
        this.gasAnalysis = new GasAnalysisController(gasFunctions);
        this.movement = new MovementController(vehicle, clock);
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    /**
     * Translate the gas-analysis subsystem's shared outputs into the
     * movement subsystem's input events and dispatch them.
     */
    public void route() {
        List<OutputEvent> emitted = gasAnalysis.drain();
        for (int i = 0; i < emitted.size(); i++) {
            OutputEvent oe = emitted.get(i);
            if (oe instanceof OutputEvent.Turn) {
                OutputEvent.Turn te = (OutputEvent.Turn) oe;
                movement.step(new InputEvent.Turn(te.a()));
            } else if (oe instanceof OutputEvent.Stop) {
                movement.step(new InputEvent.Stop());
            } else if (oe instanceof OutputEvent.Resume) {
                movement.step(new InputEvent.Resume());
            }
        }
    }
}
