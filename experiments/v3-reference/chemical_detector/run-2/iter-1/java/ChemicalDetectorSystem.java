package chemical_detector;

import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.sensor.ChemicalSensor;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.VehicleSensor;

/**
 * The Chemical Detector robot (CD-ARCH1, CD-ARCH2): the Vehicle's sensing and actuation
 * surface, the gas-analysis subsystem and the movement subsystem, wired together.
 */
public final class ChemicalDetectorSystem {

    private final Clock timer = new Clock();
    private final VehicleSensor vehicleSensor = new VehicleSensor();
    private final VehicleActuator vehicle = new VehicleActuator();
    private final MovementController movement;
    private final GasAnalysisController gasAnalysis;

    /**
     * @param target the chemical whose source the robot searches for
     */
    public ChemicalDetectorSystem(Chem target) {
        this.movement = new MovementController(vehicleSensor, timer, vehicle);
        this.gasAnalysis = new GasAnalysisController(new ChemicalSensor(target), new GasAnalysisActuator(movement));
    }

    /** Delivers a Vehicle event: gas readings to gas analysis, obstacles to movement. */
    public void deliver(InputEvent signal) {
        if (signal instanceof InputEvent.Gas) {
            gasAnalysis.step(signal);
        } else if (signal instanceof InputEvent.Obstacle) {
            movement.step(signal);
        }
    }

    /**
     * Runs one control cycle in which the Vehicle reports no event: time advances by one
     * cycle, the odometer is updated, and both subsystems take their autonomous transitions.
     */
    public void cycle(@RoboChartType("real") double distanceTravelled) {
        timer.advance();
        vehicleSensor.update(distanceTravelled);
        gasAnalysis.step(null);
        movement.step(null);
    }
}
