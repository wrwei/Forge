package chemical_detector;

import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * The Chemical Detector (CD-ARCH1, CD-ARCH2): the Vehicle's sensors and
 * actuator, the gas-analysis subsystem and the movement subsystem. The two
 * subsystems communicate only through turn, stop and resume, which this
 * class forwards from the gas-analysis subsystem to the movement subsystem.
 */
public final class ChemicalDetectorSystem {

    /** Cycles without an event that let either subsystem settle (its longest autonomous chain is 2). */
    @RoboChartType("nat") private static final int SETTLE_CYCLES = 3;

    private final GasAnalysisActuator analysisLink;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    /** Assembles the system around the Vehicle's sensors and actuator. */
    public ChemicalDetectorSystem(ChemicalSensor chemicalSensor, OdometerSensor odometerSensor,
            VehicleActuator vehicle, Clock timer) {
        this.analysisLink = new GasAnalysisActuator();
        this.gasAnalysis = new GasAnalysisController(chemicalSensor, analysisLink);
        this.movement = new MovementController(odometerSensor, vehicle, timer);
    }

    /**
     * Delivers one event from the Vehicle: a gas reading to the gas-analysis
     * subsystem, any other event to the movement subsystem; then runs the
     * cycles without an event that complete the resulting behaviour.
     */
    public void dispatch(InputEvent input) {
        if (input instanceof InputEvent.Gas) {
            gasAnalysis.step(input);
            forwardAnalysisOutput();
            for (int k = 0; k < SETTLE_CYCLES; k++) {
                gasAnalysis.step(null);
                forwardAnalysisOutput();
            }
        } else {
            movement.step(input);
            settleMovement();
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

    private void forwardAnalysisOutput() {
        OutputEvent pending = analysisLink.takePending();
        if (pending instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) pending;
            movement.step(new InputEvent.Turn(turn.direction()));
            settleMovement();
        } else if (pending instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
            settleMovement();
        } else if (pending instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
            settleMovement();
        }
    }

    private void settleMovement() {
        for (int k = 0; k < SETTLE_CYCLES; k++) {
            movement.step(null);
        }
    }
}
