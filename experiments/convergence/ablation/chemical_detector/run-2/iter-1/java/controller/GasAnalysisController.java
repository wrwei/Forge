package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.GasSensorArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and
 * decides whether to keep searching (resume), steer toward a stronger
 * signal (turn), or stop because the source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensorArray sensor;
    private final Actuator actuator;

    /** Most recent gas reading; initially empty (CD-GA-Var1). */
    private List<GasSensor> gs = new ArrayList<>();
    /** Classification of the most recent reading (CD-GA-Var2). */
    private Status sts = Status.noGas;
    /** Peak intensity of the most recent reading (CD-GA-Var3). */
    @RoboChartType("real")
    private double ins = 0.0;
    /** Direction of the strongest detected signal (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorArray sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsIsNoGas = this.sts == Status.noGas;
        boolean stsIsGasD = this.sts == Status.gasD;
        boolean insAtOrAboveThr = this.ins >= Constants.thr;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                this.gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                this.sts = sensor.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                this.ins = sensor.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                this.anl = sensor.location(this.gs);
                actuator.apply(new OutputEvent.Turn(this.anl));
            }
        }
    }
}
