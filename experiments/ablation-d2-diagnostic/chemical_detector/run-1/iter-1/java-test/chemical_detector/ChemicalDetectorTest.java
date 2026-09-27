package chemical_detector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import chemical_detector.types.Angle;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Loc;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChemicalDetectorTest {

    private static final Chem TARGET = new Chem(1);
    private static final Chem OTHER = new Chem(2);

    private static List<GasSensor> weakSignalFromRight() {
        return List.of(new GasSensor(TARGET, 0.5), new GasSensor(TARGET, 1.5),
                new GasSensor(TARGET, 0.2), new GasSensor(TARGET, 0.1));
    }

    private static List<GasSensor> strongSignal() {
        return List.of(new GasSensor(TARGET, 0.5), new GasSensor(TARGET, 4.0));
    }

    @Test
    void startsInInitialStates() {
        var cd = new ChemicalDetector(TARGET);
        assertEquals(GasAnalysisMode.Reading, cd.gasAnalysis().currentMode());
        assertEquals(MovementMode.Waiting, cd.movement().currentMode());
    }

    @Test
    void noGasReadingKeepsSearching() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(List.of(new GasSensor(OTHER, 9.0)));
        assertEquals(GasAnalysisMode.Reading, cd.gasAnalysis().currentMode());
        assertEquals(MovementMode.Waiting, cd.movement().currentMode());
        assertTrue(cd.vehicle().randomWalking());
    }

    @Test
    void weakSignalSteersTowardsStrongestSensor() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        assertEquals(GasAnalysisMode.Reading, cd.gasAnalysis().currentMode());
        assertEquals(Angle.Right, cd.gasAnalysis().anl());
        assertEquals(MovementMode.Going, cd.movement().currentMode());
        assertEquals(Angle.Right, cd.vehicle().heading());
        assertEquals(1.0, cd.vehicle().velocity());
    }

    @Test
    void strongSignalHaltsAndFlagsOnce() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        cd.receiveGas(strongSignal());
        assertEquals(GasAnalysisMode.Concluded, cd.gasAnalysis().currentMode());
        assertEquals(MovementMode.Found, cd.movement().currentMode());
        assertEquals(0.0, cd.vehicle().velocity());
        assertEquals(List.of(new OutputEvent.flag()), cd.drainVehicleSignals());

        cd.receiveGas(strongSignal());
        cd.receiveObstacle(Loc.left);
        assertEquals(MovementMode.Found, cd.movement().currentMode());
        assertEquals(List.of(), cd.drainVehicleSignals());
        assertEquals(0.0, cd.vehicle().velocity());
    }

    @Test
    void obstacleWhileGoingTriggersAvoidance() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        cd.sensors().updateOdometer(4.0);
        cd.receiveObstacle(Loc.left);
        assertEquals(MovementMode.Avoiding, cd.movement().currentMode());
        assertEquals(4.0, cd.movement().d0());
        assertEquals(Angle.Right, cd.vehicle().heading());
        cd.receiveGas(weakSignalFromRight());
        assertEquals(MovementMode.TryingAgain, cd.movement().currentMode());
    }

    @Test
    void secondObstacleSoonReturnsToAvoiding() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        cd.receiveObstacle(Loc.front);
        cd.receiveGas(weakSignalFromRight());
        cd.receiveObstacle(Loc.right);
        assertEquals(MovementMode.Avoiding, cd.movement().currentMode());
        assertEquals(Angle.Left, cd.vehicle().heading());
    }

    @Test
    void stuckRobotGetsOut() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        cd.receiveObstacle(Loc.front);
        cd.receiveGas(weakSignalFromRight());
        cd.idle(10);
        cd.receiveObstacle(Loc.front);
        assertEquals(MovementMode.GettingOut, cd.movement().currentMode());
        assertTrue(cd.vehicle().shortRandomWalking());
        cd.receiveGas(weakSignalFromRight());
        assertEquals(MovementMode.Going, cd.movement().currentMode());
    }

    @Test
    void progressingRobotKeepsAvoidingAfterStuckPeriod() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        cd.receiveObstacle(Loc.front);
        cd.receiveGas(weakSignalFromRight());
        cd.idle(10);
        cd.sensors().updateOdometer(5.0);
        cd.receiveObstacle(Loc.front);
        assertEquals(MovementMode.Avoiding, cd.movement().currentMode());
    }

    @Test
    void noGasResumesSearchFromAnyMotion() {
        var cd = new ChemicalDetector(TARGET);
        cd.receiveGas(weakSignalFromRight());
        cd.receiveObstacle(Loc.front);
        cd.receiveGas(List.of(new GasSensor(OTHER, 1.0)));
        assertEquals(MovementMode.Waiting, cd.movement().currentMode());
    }
}
