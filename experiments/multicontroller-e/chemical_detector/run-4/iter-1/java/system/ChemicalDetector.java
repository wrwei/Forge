package chemical_detector.system;

import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import java.util.List;

/**
 * The complete Chemical Detector: the Vehicle plus the gas-analysis and movement
 * subsystems, which communicate only through turn, stop and resume. Platform
 * callbacks deliver sensor input; after each input both subsystems are run until
 * no autonomous transition remains enabled.
 */
public final class ChemicalDetector {

    private static final int SETTLE_BOUND = 16;

    private final Sensor sensor;
    private final Vehicle vehicle;
    private final Clock cycleClock;
    private final MovementController movement;
    private final GasAnalysisController gasAnalysis;

    public ChemicalDetector(Chem targetChem) {
        this.sensor = new Sensor(targetChem);
        this.vehicle = new Vehicle();
        this.cycleClock = new Clock();
        this.movement = new MovementController(sensor, vehicle, new ChangeDirection(vehicle),
                vehicle, cycleClock);
        this.gasAnalysis = new GasAnalysisController(sensor, new MovementLink(movement));
        settle();
    }

    /** A new multi-sensor gas reading from the Vehicle. */
    public void gas(List<GasSensor> reading) {
        settle();
        gasAnalysis.step(new InputEvent.Gas(reading));
        settle();
    }

    /** An obstacle detected by the Vehicle on side {@code side}. */
    public void obstacle(Loc side) {
        settle();
        movement.step(new InputEvent.Obstacle(side));
        settle();
    }

    /** The latest cumulative distance reported by the odometer. */
    public void odometer(double distance) {
        sensor.updateOdometer(distance);
    }

    /** Advances time and runs any autonomous transitions that became enabled. */
    public void tick(long elapsed) {
        cycleClock.advance(elapsed);
        settle();
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public GasAnalysisMode gasAnalysisMode() {
        return gasAnalysis.currentMode();
    }

    public MovementMode movementMode() {
        return movement.currentMode();
    }

    private void settle() {
        for (int i = 0; i < SETTLE_BOUND; i++) {
            GasAnalysisMode gasBefore = gasAnalysis.currentMode();
            MovementMode movementBefore = movement.currentMode();
            movement.step(null);
            gasAnalysis.step(null);
            if (gasBefore == gasAnalysis.currentMode() && movementBefore == movement.currentMode()) {
                return;
            }
        }
    }
}
