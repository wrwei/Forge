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
 * The Last Response Engine safety controller (LRE-ARCH1, LRE-ARCH2).
 * A single-method, mode-nested if-else state machine over the four
 * operating modes OCM, MOM, HCM, and CAM. On power-up the LRE is in OCM
 * (LRE-Beh1).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private boolean inOpez;
    @RoboChartType("real")
    private double hvel;
    @RoboChartType("real")
    private double vvel;
    @RoboChartType("real")
    private double vel;
    @RoboChartType("nat")
    private int cstc;
    @RoboChartType("nat")
    private int cdyn;
    @RoboChartType("real")
    private double cda;
    @RoboChartType("real")
    private double tcpa;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOPEZ;
    private final CalcCPA calcCPA;

    public LreController(Sensor sensor, Actuator actuator, CalcVel calcVel,
                         CalcCStc calcCStc, CalcCDyn calcCDyn,
                         CheckOPEZ checkOPEZ, CalcCPA calcCPA) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = calcVel;
        this.calcCStc = calcCStc;
        this.calcCDyn = calcCDyn;
        this.checkOPEZ = checkOPEZ;
        this.calcCPA = calcCPA;
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

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOPEZ.inOpez();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        boolean velAtOrBelowOne = vel <= 1.0;
        boolean hvelAtOrAboveOne = hvel >= 1.0;
        boolean vvelAtOrAboveOne = vvel >= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafeDist = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean camCondition = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean hdistCstcAtOrBelowHorizDist = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtOrBelowVertDist = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtOrBelowDfltVertDist = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                actuator.apply(new OutputEvent.advVel(rv.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                actuator.apply(new OutputEvent.advHdng(rh.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtOrBelowOne && !inOpez
                    && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.tick && inOpez) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && !inOpez && camCondition) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.tick && !inOpez && !camCondition
                    && hvelAtOrAboveOne && hdistCstcAtOrBelowHorizDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.tick && !inOpez && !camCondition
                    && vdistCstcAtOrBelowDfltVertDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.tick && !inOpez && !camCondition
                    && vvelAtOrAboveOne && vdistCstcAtOrBelowVertDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && inOpez) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && !inOpez && camCondition) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.tick && !inOpez && !camCondition
                    && !hdistCstcAtOrBelowHorizDist && !vdistCstcAtOrBelowVertDist) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && !cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
