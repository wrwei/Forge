package chemical_detector;

import chemical_detector.actuator.CommandActuator;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * The Chemical Detector (CD-ARCH1, CD-ARCH2): the gas-analysis and movement
 * subsystems on one Vehicle. The two subsystems communicate only through the
 * turn, stop and resume commands the gas-analysis subsystem issues.
 */
public final class ChemicalDetectorSystem {
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;
    private final CommandActuator commands;

    /**
     * @param chemicalSensor the functions over gas readings
     * @param odometer       the Vehicle's odometer
     * @param vehicle        the Vehicle's movement operations
     * @param timer          the time source of the evasion timer
     */
    public ChemicalDetectorSystem(ChemicalSensor chemicalSensor, OdometerSensor odometer,
                                  VehicleActuator vehicle, Clock timer) {
        this.commands = new CommandActuator();
        this.gasAnalysis = new GasAnalysisController(chemicalSensor, commands);
        this.movement = new MovementController(vehicle, odometer, timer);
    }

    /**
     * One control cycle of the robot: gives the Vehicle's event of this cycle
     * (a gas reading, an obstacle, or {@code null} for none) to both subsystems,
     * then delivers every command the gas-analysis subsystem issued to the
     * movement subsystem, in order.
     */
    public void cycle(InputEvent platformEvent) {
        gasAnalysis.step(platformEvent);
        movement.step(platformEvent);
        for (OutputEvent command : commands.takeIssued()) {
            movement.step(toMovementEvent(command));
        }
    }

    /** The gas-analysis subsystem. */
    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    /** The movement subsystem. */
    public MovementController movement() {
        return movement;
    }

    // The commands issued are turn, stop and resume only.
    private InputEvent toMovementEvent(OutputEvent command) {
        if (command instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) command;
            return new InputEvent.Turn(turn.direction());
        } else if (command instanceof OutputEvent.Stop) {
            return new InputEvent.Stop();
        }
        return new InputEvent.Resume();
    }
}
