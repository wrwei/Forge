package chemical_detector.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChemicalDetectorTest {

    private static final Chem TARGET = new Chem(7);

    private final ChemicalDetector detector = new ChemicalDetector(TARGET);

    @Test
    void searchesHomesInAndStopsAtTheSource() {
        detector.deliver(new InputEvent.Gas(List.of(new GasSample(new Chem(3), 2.0))));
        assertEquals(GasAnalysisMode.Reading, detector.gasAnalysis().currentMode());
        assertEquals(MovementMode.Waiting, detector.movement().currentMode());
        assertTrue(detector.vehicle().randomWalking());

        detector.deliver(new InputEvent.Gas(List.of(new GasSample(TARGET, 1.0), new GasSample(TARGET, 3.0))));
        assertEquals(GasAnalysisMode.Reading, detector.gasAnalysis().currentMode());
        assertEquals(MovementMode.Going, detector.movement().currentMode());
        assertEquals(Angle.Right, detector.vehicle().heading());
        assertEquals(ChemConstants.LV, detector.vehicle().velocity());

        detector.deliver(new InputEvent.Obstacle(Loc.front));
        assertEquals(MovementMode.Avoiding, detector.movement().currentMode());
        assertEquals(Angle.Back, detector.vehicle().heading());

        detector.deliver(new InputEvent.Gas(List.of(new GasSample(TARGET, ChemConstants.THR))));
        assertEquals(GasAnalysisMode.Concluded, detector.gasAnalysis().currentMode());
        assertEquals(MovementMode.Found, detector.movement().currentMode());
        assertEquals(1, detector.vehicle().flags());
        assertEquals(0.0, detector.vehicle().velocity());

        detector.deliver(new InputEvent.Gas(List.of(new GasSample(TARGET, 9.0))));
        assertEquals(MovementMode.Found, detector.movement().currentMode());
        assertEquals(1, detector.vehicle().flags());
    }

    @Test
    void noGasReadingReturnsMovementToSearch() {
        detector.deliver(new InputEvent.Gas(List.of(new GasSample(TARGET, 1.0))));
        assertEquals(MovementMode.Going, detector.movement().currentMode());

        detector.deliver(new InputEvent.Gas(List.of()));
        assertEquals(MovementMode.Waiting, detector.movement().currentMode());
        assertTrue(detector.vehicle().randomWalking());
    }

    @Test
    void stuckDetectionUsesElapsedTimeAndProgress() {
        detector.deliver(new InputEvent.Gas(List.of(new GasSample(TARGET, 1.0))));
        detector.odometer(5.0);
        detector.deliver(new InputEvent.Obstacle(Loc.left));
        detector.deliver(new InputEvent.Gas(List.of(new GasSample(TARGET, 1.0))));
        assertEquals(MovementMode.TryingAgain, detector.movement().currentMode());

        detector.advanceTime(ChemConstants.STUCK_PERIOD);
        detector.odometer(5.5);
        detector.deliver(new InputEvent.Obstacle(Loc.right));
        assertEquals(MovementMode.GettingOut, detector.movement().currentMode());
        assertTrue(detector.vehicle().shortRandomWalking());
    }
}
