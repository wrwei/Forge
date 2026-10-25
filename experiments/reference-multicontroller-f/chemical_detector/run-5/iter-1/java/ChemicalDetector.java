package chemical_detector;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysis;
import chemical_detector.controller.Movement;
import chemical_detector.data.Chem;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.timing.Clock;

/**
 * The Chemical Detector system: the Vehicle plus the gas-analysis and movement subsystems, which
 * communicate only through the turn, stop and resume events.
 */
public final class ChemicalDetector {

    private static final int SETTLE_STEPS = 3;

    private final VehicleSensors sensors;

    private final Vehicle vehicle;

    private final Clock timer;

    private final Actuator gasAnalysisPort;

    private final Actuator movementPort;

    private final GasAnalysis gasAnalysis;

    private final Movement movement;

    private boolean flagRaised;

    public ChemicalDetector(Chem target) {
        this.sensors = new VehicleSensors(target);
        this.vehicle = new Vehicle();
        this.timer = new Clock();
        this.gasAnalysisPort = new Actuator();
        this.movementPort = new Actuator();
        this.gasAnalysis = new GasAnalysis(sensors, gasAnalysisPort);
        this.movement = new Movement(vehicle, sensors, new ChangeDirection(vehicle), movementPort, timer);
    }

    /** Delivers one Vehicle event (gas, obstacle or tick), then lets autonomous transitions settle. */
    public void cycle(InputEvent vehicleEvent) {
        deliver(vehicleEvent);
        for (int k = 0; k < SETTLE_STEPS; k++) {
            deliver(new InputEvent.Tick());
        }
    }

    public VehicleSensors sensors() {
        return sensors;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public Clock timer() {
        return timer;
    }

    public GasAnalysis gasAnalysis() {
        return gasAnalysis;
    }

    public Movement movement() {
        return movement;
    }

    /** True once the movement subsystem has emitted flag. */
    public boolean flagRaised() {
        return flagRaised;
    }

    private void deliver(InputEvent inputEvent) {
        gasAnalysis.step(inputEvent);
        for (OutputEvent sent : gasAnalysisPort.drain()) {
            movement.step(toMovementInput(sent));
        }
        movement.step(inputEvent);
        for (OutputEvent sent : movementPort.drain()) {
            if (sent instanceof OutputEvent.Flag) {
                flagRaised = true;
            }
        }
    }

    private static InputEvent toMovementInput(OutputEvent sent) {
        if (sent instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) sent;
            return new InputEvent.Turn(turn.angle());
        } else if (sent instanceof OutputEvent.Stop) {
            return new InputEvent.Stop();
        } else if (sent instanceof OutputEvent.Resume) {
            return new InputEvent.Resume();
        }
        return new InputEvent.Tick();
    }
}
