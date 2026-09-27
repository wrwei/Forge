package chemical_detector.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.actuator.Vehicle;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChemicalDetectorSystemTest {

    private static final Chem TARGET = new Chem(1);
    private static final InputEvent IDLE = new InputEvent.NoEvent();

    private final Vehicle vehicle = new Vehicle();
    private final ChemicalDetectorSystem system =
            new ChemicalDetectorSystem(new Sensor(TARGET), vehicle, new Clock());

    private void analyse(GasSample... samples) {
        system.cycle(new InputEvent.Gas(List.of(samples)));
        system.cycle(IDLE);
        system.cycle(IDLE);
    }

    @Test
    void homesInOnWeakSignalThenHaltsAtSource() {
        analyse(new GasSample(TARGET, 0.0), new GasSample(TARGET, 0.4));
        assertEquals(MovementMode.Going, system.movement().currentMode());
        assertEquals(Angle.Right, vehicle.direction());
        assertFalse(system.sourceFound());

        analyse(new GasSample(TARGET, 1.5));
        assertEquals(GasAnalysisMode.Concluded, system.gasAnalysis().currentMode());
        assertEquals(MovementMode.Found, system.movement().currentMode());
        assertTrue(system.sourceFound());
        assertEquals(0.0, vehicle.velocity());
    }

    @Test
    void noGasKeepsSearching() {
        analyse(new GasSample(new Chem(7), 5.0));
        assertEquals(GasAnalysisMode.Reading, system.gasAnalysis().currentMode());
        assertEquals(MovementMode.Waiting, system.movement().currentMode());
        assertTrue(vehicle.randomWalking());
    }
}
