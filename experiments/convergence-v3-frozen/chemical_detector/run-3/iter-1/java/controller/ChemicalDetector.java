package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.event.GasAnalysisEvent;
import chemical_detector.event.MovementBus;
import chemical_detector.event.MovementEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.GasFunctions;

/**
 * The Chemical Detector: composes the gas-analysis and movement subsystems,
 * which communicate only through the {@link MovementBus} (turn, stop, resume),
 * and runs them in a sense-analyse-act loop.
 */
public final class ChemicalDetector {

    private final Clock clock;
    private final MovementBus bus;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetector(Vehicle vehicle, Clock clock, GasFunctions functions) {
        this.clock = clock;
        this.bus = new MovementBus();
        this.gasAnalysis = new GasAnalysisController(functions, this.bus);
        this.movement = new MovementController(vehicle, new ChangeDirection(vehicle), clock);
    }

    /**
     * Runs one control cycle: the Vehicle's obstacle report (or NoCommand) is
     * handled by the movement subsystem, the gas reading (or NoReading) by the
     * gas-analysis subsystem, whose resulting command is then delivered to the
     * movement subsystem; finally one time unit passes.
     */
    public void cycle(GasAnalysisEvent reading, MovementEvent detection) {
        this.movement.step(detection);
        this.gasAnalysis.step(reading);
        this.movement.step(this.bus.take());
        this.clock.advance(1);
    }

    public GasAnalysisController gasAnalysis() {
        return this.gasAnalysis;
    }

    public MovementController movement() {
        return this.movement;
    }
}
