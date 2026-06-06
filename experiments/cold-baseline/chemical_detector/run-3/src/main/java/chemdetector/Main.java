package chemdetector;

import chemdetector.controller.GasAnalysisController;
import chemdetector.controller.MovementController;
import chemdetector.data.Chem;
import chemdetector.data.GasSensor;
import chemdetector.event.GAInputEvent;
import chemdetector.event.MVInputEvent;
import chemdetector.event.SharedEventBus;
import chemdetector.sensor.GasFunctions;
import chemdetector.sensor.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo wiring of the Chemical Detector system. Drives one
 * search/classification cycle culminating in a chemical-source
 * detection above threshold.
 */
public final class Main {

    public static void main(String[] args) {
        var vehicle = new Vehicle();
        var clock = new Clock();
        var gasFunctions = new GasFunctions();
        var bus = new SharedEventBus();
        var gaController = new GasAnalysisController(gasFunctions, bus);
        var mvController = new MovementController(vehicle, clock);

        // Build a gas reading whose peak intensity is above the threshold
        // (CD-Const1 thr = 80.0).
        List<GasSensor> reading = new ArrayList<GasSensor>();
        reading.add(new GasSensor(new Chem(2), 10.0));
        reading.add(new GasSensor(new Chem(1), 95.0));
        reading.add(new GasSensor(new Chem(1), 40.0));
        reading.add(new GasSensor(new Chem(2), 5.0));

        // Drive the GA controller: Reading -> Analysis -> GasDetected -> Final.
        gaController.step(new GAInputEvent.Gas(reading));
        gaController.step(null);
        gaController.step(null);

        // GA emitted stop on the bus; deliver it to the MV controller.
        mvController.step(new MVInputEvent.Stop());
        mvController.step(null);

        System.out.println("ga mode: " + gaController.currentMode());
        System.out.println("mv mode: " + mvController.currentMode());
        System.out.println("flag: " + vehicle.flagSignalled());
    }

    private Main() {
    }
}
