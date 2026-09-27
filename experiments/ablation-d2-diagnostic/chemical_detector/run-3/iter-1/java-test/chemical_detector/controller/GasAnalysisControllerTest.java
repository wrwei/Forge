package chemical_detector.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.actuator.Actuator;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import java.util.List;
import org.junit.jupiter.api.Test;

class GasAnalysisControllerTest {

    private static final Chem TARGET = new Chem(1);
    private static final InputEvent IDLE = new InputEvent.NoEvent();

    private final Actuator out = new Actuator();
    private final GasAnalysisController ga = new GasAnalysisController(new Sensor(TARGET), out);

    private static InputEvent gas(GasSample... samples) {
        return new InputEvent.Gas(List.of(samples));
    }

    @Test
    void readingWaitsForGas() {
        ga.step(IDLE);
        assertEquals(GasAnalysisMode.Reading, ga.currentMode());
        assertTrue(out.drain().isEmpty());
    }

    @Test
    void noGasReadingResumesSearchAndReturnsToReading() {
        ga.step(gas(new GasSample(new Chem(9), 2.0)));
        assertEquals(GasAnalysisMode.Analysis, ga.currentMode());
        assertEquals(Status.noGas, ga.sts());

        ga.step(IDLE);
        assertEquals(GasAnalysisMode.NoGas, ga.currentMode());
        assertEquals(List.of(new OutputEvent.Resume()), out.drain());

        ga.step(IDLE);
        assertEquals(GasAnalysisMode.Reading, ga.currentMode());
    }

    @Test
    void weakSignalTurnsTowardsPeakAndKeepsReading() {
        ga.step(gas(new GasSample(TARGET, 0.1), new GasSample(TARGET, 0.6)));
        ga.step(IDLE);
        assertEquals(GasAnalysisMode.GasDetected, ga.currentMode());
        assertEquals(0.6, ga.ins());

        ga.step(IDLE);
        assertEquals(GasAnalysisMode.Reading, ga.currentMode());
        assertEquals(Angle.Right, ga.anl());
        assertEquals(List.of(new OutputEvent.Turn(Angle.Right)), out.drain());
    }

    @Test
    void strongSignalStopsOnceAndIgnoresLaterReadings() {
        ga.step(gas(new GasSample(TARGET, 1.0)));
        ga.step(IDLE);
        ga.step(IDLE);
        assertEquals(GasAnalysisMode.Concluded, ga.currentMode());
        assertEquals(List.of(new OutputEvent.Stop()), out.drain());

        var later = List.of(new GasSample(TARGET, 0.2));
        ga.step(new InputEvent.Gas(later));
        ga.step(IDLE);
        assertEquals(GasAnalysisMode.Concluded, ga.currentMode());
        assertEquals(later, ga.gs());
        assertTrue(out.drain().isEmpty());
    }
}
