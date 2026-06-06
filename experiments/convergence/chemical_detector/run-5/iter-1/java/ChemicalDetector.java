package chemdetector;

import java.util.List;

import chemdetector.actuator.Vehicle;
import chemdetector.controller.GasAnalysisController;
import chemdetector.controller.MovementController;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.sensor.ChemSensorService;
import chemdetector.sensor.Clock;

/**
 * Top-level composition of the Chemical Detector: the Vehicle surface,
 * the gas-analysis subsystem, and the movement subsystem (CD-ARCH1,
 * CD-ARCH2). The two reasoning subsystems communicate only via the
 * turn, stop, and resume events carried by the SignalPort.
 */
public final class ChemicalDetector {

    private final Vehicle vehicle = new Vehicle();
    private final Clock clock = new Clock();
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetector(Chem targetChem) {
        this.gasAnalysis = new GasAnalysisController(new ChemSensorService(targetChem));
        this.movement = new MovementController(vehicle, clock);
    }

    /** Delivers one multi-sensor gas reading (CD-Evt1). */
    public void onGasReading(List<GasSensor> reading) {
        gasAnalysis.step(new GasAnalysisEvent.Gas(reading));
        forwardSignals();
    }

    /** Delivers an obstacle notification (CD-Evt2). */
    public void onObstacle(Loc side) {
        movement.step(new MovementEvent.Obstacle(side));
    }

    /** Runs one autonomous control cycle with no external event. */
    public void cycle() {
        gasAnalysis.step(null);
        forwardSignals();
        movement.step(null);
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

    private void forwardSignals() {
        List<MovementEvent> out = gasAnalysis.signals().drain();
        for (MovementEvent e : out) {
            movement.step(e);
        }
    }
}
