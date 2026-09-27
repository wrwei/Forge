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
    void analysisDetectsTargetWithPositiveIntensity() {
        assertEquals(Status.gasD, sensor.analysis(List.of(new GasSample(OTHER, 3.0), new GasSample(TARGET, 0.5))));
    }

    @Test
    void analysisIgnoresOtherChemicalsAndZeroIntensity() {
        assertEquals(Status.noGas, sensor.analysis(List.of(new GasSample(OTHER, 3.0), new GasSample(TARGET, 0.0))));
        assertEquals(Status.noGas, sensor.analysis(List.of()));
    }

    @Test
    void intensityIsPeakOfReading() {
        var gs = List.of(new GasSample(TARGET, 0.2), new GasSample(TARGET, 1.7), new GasSample(OTHER, 0.9));
        assertEquals(1.7, sensor.intensity(gs));
        assertEquals(0.0, sensor.intensity(List.of()));
    }

    @Test
    void locationIsDirectionOfFirstPeakSensor() {
        var gs = List.of(new GasSample(TARGET, 0.2), new GasSample(TARGET, 0.4),
                new GasSample(TARGET, 1.1), new GasSample(TARGET, 1.1));
        assertEquals(Angle.Back, sensor.location(gs));
        assertEquals(Angle.Left, sensor.location(List.of(new GasSample(TARGET, 0.3))));
        assertEquals(Angle.Front, sensor.location(List.of()));
    }

    @Test
    void goreqIsAtLeast() {
        assertTrue(sensor.goreq(1.0, 1.0));
        assertTrue(sensor.goreq(2.0, 1.0));
        assertFalse(sensor.goreq(0.5, 1.0));
    }

    @Test
    void odometerReportsLatestDistance() {
        sensor.updateOdometer(4.5);
        assertEquals(4.5, sensor.odometer());
    }
}
