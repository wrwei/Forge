package lre.controller;

import lre.actuator.Actuator;
import lre.constants.LreConstants;
import lre.event.InputEvent;
import lre.event.OutputEvent;
import lre.mode.LreMode;
import lre.operation.CalcCDyn;
import lre.operation.CalcCPA;
import lre.operation.CalcCStc;
import lre.operation.CalcVel;
import lre.operation.CheckOPEZ;
import lre.sensor.Sensor;

/**
 * The Last Response Engine: a reactive safety controller mediating between the
 * operator controller and the AUV autopilot controller.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOPEZ;
    private final CalcCPA calcCPA;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtMostOne = calcVel.vel() <= 1.0;
        boolean hvelAtLeastOne = calcVel.hvel() >= 1.0;
        boolean vvelAtLeastOne = calcVel.vvel() >= 1.0;
        boolean odistCstcAboveOne = calcCStc.odistCstc() > 1.0;
        boolean odistCdynAboveOne = calcCDyn.odistCdyn() > 1.0;
        boolean hdistCstcAtMostHoriz = calcCStc.hdistCstc() <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostVert = calcCStc.vdistCstc() <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtMostDfltVert = calcCStc.vdistCstc() <= LreConstants.staticObsDfltVertDist;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel reqVel = (InputEvent.ReqVel) event;
                actuator.apply(new OutputEvent.AdvVel(reqVel.value()));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng reqHdng = (InputEvent.ReqHdng) event;
                actuator.apply(new OutputEvent.AdvHdng(reqHdng.value()));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqMOM && velAtMostOne && !inOpez
                    && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hvelAtLeastOne && hdistCstcAtMostHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vdistCstcAtMostDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vvelAtLeastOne && vdistCstcAtMostVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (!hdistCstcAtMostHoriz && !vdistCstcAtMostVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
