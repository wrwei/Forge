package chemical_detector;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;
import java.util.List;

/**
 * Wires the two reasoning subsystems together: Vehicle inputs go to the subsystem that
 * consumes them, and gas analysis talks to movement only through turn, stop and resume.
 */
public final class ChemicalDetector {

    /** Enough autonomous cycles for gas analysis to pass Analysis and GasDetected/NoGas. */
    private static final int SETTLE_ROUNDS = 3;

    private final Actuator gasAnalysisOutput = new Actuator();
    private final Actuator movementOutput = new Actuator();
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;
    private boolean flagRaised;

    public ChemicalDetector(GasSensorArray gasSensors, OdometerSensor odometer, Vehicle vehicle, Clock timer) {
        this.gasAnalysis = new GasAnalysisController(gasSensors, gasAnalysisOutput);
        this.movement = new MovementController(odometer, vehicle, movementOutput, timer);
    }

    /** Delivers one multi-sensor gas reading from the Vehicle. */
    public void gas(List<GasSensor> readings) {
        gasAnalysis.step(new InputEvent.Gas(List.copyOf(readings)));
        settle();
    }

    /** Delivers an obstacle detection from the Vehicle. */
    public void obstacle(Loc side) {
        movement.step(new InputEvent.Obstacle(side));
        settle();
    }

    /** Runs control cycles with no new Vehicle input. */
    public void tick() {
        settle();
    }

    /** True once the movement subsystem has sent the flag event. */
    public boolean flagRaised() {
        return flagRaised;
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    private void settle() {
        for (var round = 0; round < SETTLE_ROUNDS; round++) {
            gasAnalysis.step(null);
            movement.step(null);
            forwardOutputs();
        }
    }

    private void forwardOutputs() {
        for (var out : gasAnalysisOutput.drain()) {
            if (out instanceof OutputEvent.Turn) {
                OutputEvent.Turn turn = (OutputEvent.Turn) out;
                movement.step(new InputEvent.Turn(turn.angle()));
            } else if (out instanceof OutputEvent.Stop) {
                movement.step(new InputEvent.Stop());
            } else if (out instanceof OutputEvent.Resume) {
                movement.step(new InputEvent.Resume());
            }
        }
        for (var out : movementOutput.drain()) {
            if (out instanceof OutputEvent.Flag) {
                flagRaised = true;
            }
        }
    }
}
