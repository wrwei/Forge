package chemical_detector;

import chemical_detector.actuator.CommandLink;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * The Chemical Detector: the gas-analysis and movement subsystems over the
 * Vehicle's sensors and movement operations. The two subsystems communicate
 * only through the turn, stop and resume signals.
 */
public final class ChemicalDetector {

    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;
    private final CommandLink link;
    private final Clock timer;

    /**
     * @param gasSensors the gas-analysis functions over the gas-sensor array
     * @param odometerSensor the Vehicle's odometer
     * @param vehicle the Vehicle's movement operations
     * @param timer the time source
     */
    public ChemicalDetector(GasSensorArray gasSensors, OdometerSensor odometerSensor,
                            VehicleActuator vehicle, Clock timer) {
        this.link = new CommandLink();
        this.timer = timer;
        this.gasAnalysis = new GasAnalysisController(gasSensors, link);
        this.movement = new MovementController(odometerSensor, vehicle, timer);
    }

    /**
     * One control cycle: delivers the Vehicle's event to both subsystems,
     * forwards the signal the gas-analysis subsystem emitted (if any) to the
     * movement subsystem, and advances time by one unit.
     *
     * @param vehicleEvent the gas or obstacle event of this cycle, or null if there is none
     */
    public void cycle(InputEvent vehicleEvent) {
        gasAnalysis.step(vehicleEvent);
        movement.step(vehicleEvent);
        if (link.hasPending()) {
            OutputEvent signal = link.take();
            if (signal instanceof OutputEvent.Turn) {
                OutputEvent.Turn turnSignal = (OutputEvent.Turn) signal;
                movement.step(new InputEvent.Turn(turnSignal.direction()));
            } else if (signal instanceof OutputEvent.Stop) {
                movement.step(new InputEvent.Stop());
            } else if (signal instanceof OutputEvent.Resume) {
                movement.step(new InputEvent.Resume());
            }
        }
        timer.tick();
    }
}
