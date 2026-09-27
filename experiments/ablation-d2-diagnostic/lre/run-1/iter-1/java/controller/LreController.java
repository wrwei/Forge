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
 * The Last Response Engine: the AUV's safety controller (LRE-ARCH1,
 * LRE-ARCH2). One cycle of the controller is one call of
 * {@link #step(InputEvent)}.
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

    /** True when the AUV is inside an Object Proximity Exclusion Zone (LRE-Var1, LRE-GP1). */
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

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
    }

    /** The mode the LRE is currently in; OCM on power-up (LRE-Beh1). */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: refresh the derived quantities, then evaluate the
     * transitions leaving the current mode in priority order.
     */
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
        boolean hdistCstcAtOrBelowHorizDist = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtOrBelowVertDist = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtOrBelowDfltVertDist = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqMOM && velAtOrBelowOne && !inOpez
                    && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            } else if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel requestedVel = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(requestedVel.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng requestedHdng = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(requestedHdng.value()));
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
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafeDist && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hvelAtOrAboveOne && hdistCstcAtOrBelowHorizDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtOrBelowDfltVertDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtOrAboveOne && vdistCstcAtOrBelowVertDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafeDist && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (!hdistCstcAtOrBelowHorizDist && !vdistCstcAtOrBelowVertDist) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
