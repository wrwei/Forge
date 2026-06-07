package lre.controller;

import lre.actuator.Actuator;
import lre.annotation.RoboChartType;
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
 * The Last Response Engine (LRE) safety controller for the AUV
 * (LRE-ARCH1, LRE-ARCH2). A single-method, mode-nested if-else state
 * machine over the four operating modes OCM, MOM, HCM, and CAM.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOpez;
    private final CalcCPA calcCPA;

    private boolean inOpez;

    @RoboChartType("real")
    private double hvel;

    @RoboChartType("real")
    private double vvel;

    @RoboChartType("real")
    private double vel;

    @RoboChartType("nat")
    private int cstc = -1;

    @RoboChartType("nat")
    private int cdyn = -1;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOpez = new CheckOPEZ(sensor, calcCStc);
        this.calcCPA = new CalcCPA(sensor, calcCDyn);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOpez.compute();
        calcCPA.compute();

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOpez.inOpez();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        boolean velAtOrBelowOne = this.vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(this.cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(this.cstc) > 1.0;
        boolean cdaBelowMinSafe = this.cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = this.tcpa >= 0.0;
        boolean hvelAtOrAboveOne = this.hvel >= 1.0;
        boolean vvelAtOrAboveOne = this.vvel >= 1.0;
        boolean hdistCstcAtOrBelowHoriz =
                sensor.hdist(this.cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtOrBelowVert =
                sensor.vdist(this.cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtOrBelowDfltVert =
                sensor.vdist(this.cstc) <= LreConstants.staticObsDfltVertDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                actuator.apply(new OutputEvent.advVel(rv.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                actuator.apply(new OutputEvent.advHdng(rh.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtOrBelowOne
                    && !inOpez && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtOrBelowDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtOrAboveOne && vdistCstcAtOrBelowVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (!hdistCstcAtOrBelowHoriz && !vdistCstcAtOrBelowVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
