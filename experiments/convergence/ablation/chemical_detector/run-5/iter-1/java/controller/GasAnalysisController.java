package chemdetector.controller;

import java.util.ArrayList;
import java.util.List;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.ChemSensor;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and
 * decides whether to keep searching (resume), steer toward a stronger
 * signal (turn), or stop because the source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final ChemSensor sensor;
    private final Actuator actuator;

    /** Most recent gas reading (CD-GA-Var1). Initially empty. */
    private List<GasSensor> gs = new ArrayList<>();

    /** Outcome of the latest classification (CD-GA-Var2). */
    private Status sts = Status.noGas;

    /** Peak intensity of the latest gasD reading (CD-GA-Var3). */
    @RoboChartType("real")
    private double ins;

    /** Direction of the strongest detected signal (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    public GasAnalysisController(ChemSensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean insAtOrAboveThr = sensor.goreq(ins, Constants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Stopped;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Stopped) {
            if (event instanceof InputEvent.Gas) {
                currentMode = GasAnalysisMode.Stopped;
            }
        }
    }
}
