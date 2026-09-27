package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import java.util.Objects;

/**
 * The Chemical Detector: a gas-analysis and a movement subsystem that
 * communicate only through the turn, stop and resume events, driven once per
 * control cycle by the platform.
 */
public final class ChemicalDetectorSystem {

    private final Actuator gasAnalysisOut = new Actuator();
    private final Actuator movementOut = new Actuator();
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;
    private boolean flagRaised;

    public ChemicalDetectorSystem(Sensor sensor, Vehicle vehicle, Clock timeSource) {
        this.gasAnalysis = new GasAnalysisController(sensor, gasAnalysisOut);
        this.movement = new MovementController(sensor, vehicle, movementOut, timeSource);
    }

    /**
     * Runs one sense-analyse-act cycle. {@code event} is what the Vehicle reported
     * this cycle (a gas reading, an obstacle, or {@link InputEvent.NoEvent}); events
     * emitted by gas analysis are then delivered to movement in order.
     */
    public void cycle(InputEvent event) {
        Objects.requireNonNull(event);
        gasAnalysis.step(event);
        movement.step(event);
        for (var out : gasAnalysisOut.drain()) {
            movement.step(toMovementInput(out));
        }
        for (var out : movementOut.drain()) {
            if (out instanceof OutputEvent.Flag) {
                flagRaised = true;
            }
        }
    }

    /** True once the movement subsystem has emitted the flag event. */
    public boolean sourceFound() {
        return flagRaised;
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    private static InputEvent toMovementInput(OutputEvent out) {
        if (out instanceof OutputEvent.Turn) {
            var turn = (OutputEvent.Turn) out;
            return new InputEvent.Turn(turn.a());
        } else if (out instanceof OutputEvent.Stop) {
            return new InputEvent.Stop();
        } else if (out instanceof OutputEvent.Resume) {
            return new InputEvent.Resume();
        }
        return new InputEvent.NoEvent();
    }
}
