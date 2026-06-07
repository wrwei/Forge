package chemdetector.controller;

import chemdetector.actuator.AnalysisRelay;
import chemdetector.actuator.Vehicle;
import chemdetector.data.GasSensor;
import chemdetector.data.Loc;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.GasAnalyzer;
import java.util.List;

/**
 * The Chemical Detector system: an autonomous mobile robot that searches
 * for the source of a target chemical. Wires the gas-analysis and
 * movement subsystems to the Vehicle and drives the continuous
 * sense-analyse-act loop.
 */
public final class ChemicalDetector {

    private final Vehicle vehicle;
    private final Clock clock;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetector() {
        this.vehicle = new Vehicle();
        this.clock = new Clock();
        GasAnalyzer analyzer = new GasAnalyzer();
        AnalysisRelay relay = new AnalysisRelay();
        this.movement = new MovementController(vehicle, clock);
        this.gasAnalysis = new GasAnalysisController(analyzer, relay);
        relay.connect(movement);
    }

    /**
     * One sense-analyse-act cycle for a fresh gas reading: the
     * gas-analysis subsystem classifies it and, through its autonomous
     * transitions, relays any turn/stop/resume decision to the movement
     * subsystem.
     */
    public void onGasReading(List<GasSensor> reading) {
        gasAnalysis.step(new GasAnalysisEvent.Gas(reading));
        for (int i = 0; i < 3; i++) {
            gasAnalysis.step(null);
        }
    }

    /** Delivers an obstacle detection to the movement subsystem. */
    public void onObstacle(Loc side) {
        movement.step(new MovementEvent.Obstacle(side));
    }

    /** Evaluates autonomous movement transitions for one cycle. */
    public void onCycle() {
        movement.step(null);
    }

    public GasAnalysisMode gasAnalysisMode() {
        return gasAnalysis.currentMode();
    }

    public MovementMode movementMode() {
        return movement.currentMode();
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public Clock clock() {
        return clock;
    }
}
