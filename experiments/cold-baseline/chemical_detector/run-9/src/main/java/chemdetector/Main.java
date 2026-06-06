package chemdetector;

import chemdetector.actuator.Clock;
import chemdetector.actuator.Vehicle;
import chemdetector.controller.GasAnalysisController;
import chemdetector.controller.MovementController;
import chemdetector.datatype.Chem;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.operation.Analysis;
import chemdetector.operation.ChangeDirection;
import chemdetector.operation.IntensityCompute;
import chemdetector.operation.LocationCompute;
import chemdetector.sensor.GasSensorService;
import chemdetector.sensor.MovementSensorService;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal demo orchestrator. Wires the two controllers and runs a short
 * scenario to demonstrate the sense-analyse-act loop.
 */
public final class Main {

    public static void main(String[] args) {
        Vehicle vehicle = new Vehicle();
        Clock clock = new Clock();
        GasSensorService gasSensor = new GasSensorService();
        MovementSensorService movementSensor = new MovementSensorService();
        Analysis analysis = new Analysis(gasSensor);
        IntensityCompute intensityCompute = new IntensityCompute();
        LocationCompute locationCompute = new LocationCompute();
        ChangeDirection changeDirection = new ChangeDirection(vehicle);

        MovementController movement = new MovementController(vehicle, movementSensor, changeDirection, clock);
        GasAnalysisController gasAnalysis = new GasAnalysisController(
                gasSensor, analysis, intensityCompute, locationCompute, movement);

        // Drive a single gas reading just above the threshold so the GA controller
        // transitions Reading -> Analysis -> GasDetected -> Final and emits stop.
        List<GasSensor> highReading = new ArrayList<>();
        highReading.add(new GasSensor(new Chem(1), new Intensity(90.0)));
        gasAnalysis.step(new GasAnalysisEvent.Gas(highReading));
        // Run twice more so Analysis -> GasDetected -> Final fires.
        gasAnalysis.step(null);
        gasAnalysis.step(null);

        // Drain the inter-controller queue into the movement controller.
        while (movement.hasPending()) {
            MovementEvent next = movement.nextPending();
            movement.step(next);
        }
        // One extra step so Found -> Final autonomous transition completes.
        movement.step(null);

        System.out.println("GA mode = " + gasAnalysis.currentMode());
        System.out.println("MV mode = " + movement.currentMode());
        System.out.println("Vehicle last op = " + vehicle.lastOp());
        System.out.println("Vehicle flag = " + vehicle.flagRaised());
    }

    private Main() {
    }
}
