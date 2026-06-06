package chemdetector;

import chemdetector.datatype.Chem;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.event.GasAnalysisInputEvent;
import chemdetector.event.MovementInputEvent;
import chemdetector.datatype.Loc;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal demo entry point. Walks the system through a short scenario:
 *   1. Empty reading (noGas) -> resume -> Waiting self-loop.
 *   2. Reading with target chemical but low intensity -> turn -> Going.
 *   3. Obstacle while Going -> Avoiding -> ... -> stop -> Found.
 */
public final class Main {

    public static void main(String[] args) {
        Chem target = new Chem(1);
        ChemDetectorSystem sys = new ChemDetectorSystem(target);

        // 1. Empty reading: classify, send resume, return to Reading; Movement self-loops in Waiting.
        List<GasSensor> empty = new ArrayList<>();
        sys.deliverGas(new GasAnalysisInputEvent.Gas(empty));
        sys.gasAnalysis().step(null);  // autonomous Analysis -> NoGas
        sys.gasAnalysis().step(null);  // autonomous NoGas -> Reading

        // 2. Reading with target but below threshold: send turn, return to Reading.
        List<GasSensor> hit = new ArrayList<>();
        hit.add(new GasSensor(new Chem(0), new Intensity(0.5)));
        hit.add(new GasSensor(target, new Intensity(3.0)));
        sys.deliverGas(new GasAnalysisInputEvent.Gas(hit));
        sys.gasAnalysis().step(null); // Analysis -> GasDetected
        sys.gasAnalysis().step(null); // GasDetected -> Reading (turn ! anl)

        // 3. Obstacle while Going.
        sys.deliverMovement(new MovementInputEvent.Obstacle(Loc.front));

        // 4. Strong reading above threshold: send stop, Movement -> Found.
        List<GasSensor> strong = new ArrayList<>();
        strong.add(new GasSensor(target, new Intensity(99.0)));
        sys.deliverGas(new GasAnalysisInputEvent.Gas(strong));
        sys.gasAnalysis().step(null); // Analysis -> GasDetected
        sys.gasAnalysis().step(null); // GasDetected -> Final (stop)
        sys.movement().step(null);    // Found -> Final autonomous
    }
}
