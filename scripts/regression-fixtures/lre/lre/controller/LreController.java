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

public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CalcCPA calcCPA;
    private final CheckOPEZ checkOPEZ;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.calcCPA = new CalcCPA(sensor, calcCDyn);
        this.checkOPEZ = new CheckOPEZ(sensor, calcCStc);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        // Invoke operations each step
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        calcCPA.compute();
        checkOPEZ.compute();

        // --- Named boolean predicates ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtOrBelow1 = calcVel.vel() <= 1.0;
        boolean hvelAtOrAbove1 = calcVel.hvel() >= 1.0;
        boolean vvelAtOrAbove1 = calcVel.vvel() >= 1.0;
        boolean odistCdynAbove1 = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCstcAbove1 = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean hdistCstcAtOrBelowHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAtOrBelowDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean vdistCstcAtOrBelowVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean hdistCstcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.STATIC_OBS_VERT_DIST;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNeg = calcCPA.tcpa() >= 0.0;
        boolean cdaAtOrAboveMinSafe = calcCPA.cda() >= LreConstants.MIN_SAFE_DIST;

        // --- Pure mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel reqVel = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(reqVel.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng reqHdng = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(reqHdng.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtOrBelow1 && !inOpez && odistCdynAbove1 && odistCstcAbove1) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNeg) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (hvelAtOrAbove1 && hdistCstcAtOrBelowHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vdistCstcAtOrBelowDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vvelAtOrAbove1 && vdistCstcAtOrBelowVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNeg) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (cdaAtOrAboveMinSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
