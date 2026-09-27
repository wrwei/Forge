package chemical_detector.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import chemical_detector.actuator.CommandChannel;
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

class GasAnalysisTest {

    private static final Chem TARGET = new Chem(1);
    private static final Chem OTHER = new Chem(2);

    private final CommandChannel commands = new CommandChannel();
    private final GasAnalysis gasAnalysis = new GasAnalysis(new Sensor(TARGET), commands);

    @Test
    void startsInReading() {
        assertEquals(GasAnalysisMode.Reading, gasAnalysis.currentMode());
        assertEquals(List.of(), gasAnalysis.gs());
    }

    @Test
    void noGasReadingResumesSearch() {
        var reading = List.of(new GasSample(OTHER, 3.0));
        gasAnalysis.step(new InputEvent.Gas(reading));
        assertEquals(GasAnalysisMode.Analysis, gasAnalysis.currentMode());
        assertEquals(Status.noGas, gasAnalysis.sts());
        assertEquals(reading, gasAnalysis.gs());

        gasAnalysis.step(null);
        assertEquals(GasAnalysisMode.NoGas, gasAnalysis.currentMode());
        assertEquals(List.of(new OutputEvent.Resume()), commands.drain());

        gasAnalysis.step(null);
        assertEquals(GasAnalysisMode.Reading, gasAnalysis.currentMode());
        assertEquals(List.of(), commands.drain());
    }

    @Test
    void weakSignalTurnsTowardsStrongestSensor() {
        var reading = List.of(new GasSample(TARGET, 1.0), new GasSample(TARGET, 2.0));
        gasAnalysis.step(new InputEvent.Gas(reading));
        gasAnalysis.step(null);
        assertEquals(GasAnalysisMode.GasDetected, gasAnalysis.currentMode());
        assertEquals(2.0, gasAnalysis.ins());

        gasAnalysis.step(null);
        assertEquals(GasAnalysisMode.Reading, gasAnalysis.currentMode());
        assertEquals(Angle.Right, gasAnalysis.anl());
        assertEquals(List.of(new OutputEvent.Turn(Angle.Right)), commands.drain());
    }

    @Test
    void strongSignalStopsAndConcludes() {
        gasAnalysis.step(new InputEvent.Gas(List.of(new GasSample(TARGET, 5.0))));
        gasAnalysis.step(null);
        gasAnalysis.step(null);
        assertEquals(GasAnalysisMode.Concluded, gasAnalysis.currentMode());
        assertEquals(List.of(new OutputEvent.Stop()), commands.drain());

        var later = List.of(new GasSample(OTHER, 1.0));
        gasAnalysis.step(new InputEvent.Gas(later));
        gasAnalysis.step(null);
        assertEquals(GasAnalysisMode.Concluded, gasAnalysis.currentMode());
        assertEquals(later, gasAnalysis.gs());
        assertEquals(Status.gasD, gasAnalysis.sts());
        assertEquals(List.of(), commands.drain());
    }
}
