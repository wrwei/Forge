package chemical_detector.sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import java.util.List;
import org.junit.jupiter.api.Test;

class SensorTest {

    private static final Chem TARGET = new Chem(1);
    private static final Chem OTHER = new Chem(2);

    private final Sensor sensor = new Sensor(TARGET);

    @Test
    void analysisDetectsTargetChemicalOnly() {
        assertEquals(Status.noGas, sensor.analysis(List.of()));
        assertEquals(Status.noGas, sensor.analysis(List.of(new GasSample(OTHER, 9.0))));
        assertEquals(Status.noGas, sensor.analysis(List.of(new GasSample(TARGET, 0.0))));
        assertEquals(Status.gasD, sensor.analysis(List.of(new GasSample(OTHER, 9.0), new GasSample(TARGET, 0.5))));
    }

    @Test
    void intensityIsThePeakOfTheReading() {
        assertEquals(0.0, sensor.intensity(List.of()));
        var reading = List.of(new GasSample(TARGET, 1.0), new GasSample(TARGET, 4.0), new GasSample(OTHER, 2.0));
        assertEquals(4.0, sensor.intensity(reading));
    }

    @Test
    void locationIsTheDirectionOfThePeakSensor() {
        assertEquals(Angle.Front, sensor.location(List.of()));
        var reading = List.of(
                new GasSample(TARGET, 1.0),
                new GasSample(TARGET, 2.0),
                new GasSample(TARGET, 7.0),
                new GasSample(TARGET, 3.0));
        assertEquals(Angle.Back, sensor.location(reading));
        assertEquals(Angle.Left, sensor.location(List.of(
                new GasSample(TARGET, 0.0),
                new GasSample(TARGET, 0.0),
                new GasSample(TARGET, 0.0),
                new GasSample(TARGET, 1.0))));
    }

    @Test
    void goreqIsAtLeast() {
        assertTrue(sensor.goreq(2.0, 2.0));
        assertTrue(sensor.goreq(3.0, 2.0));
        assertFalse(sensor.goreq(1.0, 2.0));
    }
}
