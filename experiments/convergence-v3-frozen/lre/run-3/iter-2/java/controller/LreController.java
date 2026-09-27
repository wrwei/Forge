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
 * The Last Response Engine: a reactive safety controller that mediates between
 * the operator controller and the AUV's autopilot controller.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CalcCPA calcCPA;
    private final CheckOPEZ checkOPEZ;

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
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
    }

    /** The mode the Last Response Engine is currently in. */
    public LreMode currentMode() {
        return currentMode;
    }

    /** One control cycle: recompute the derived quantities, then take at most one transition. */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        calcCPA.compute();
        checkOPEZ.compute();

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();
        this.inOpez = checkOPEZ.inOpez();

        boolean velAtOrBelowOne = vel <= 1.0;
        boolean hvelAtOrAboveOne = hvel >= 1.0;
        boolean vvelAtOrAboveOne = vvel >= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean hdistCstcAtOrBelowHoriz = sensor.hdist(cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAtOrBelowVert = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcAtOrBelowDfltVert =
                sensor.vdist(cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean cdaBelowMinSafeDist = cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = tcpa >= 0.0;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel requestedVel = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(requestedVel.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng requestedHdng = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(requestedHdng.value()));
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
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafeDist && tcpaNonNegative) {
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
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafeDist && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (!hdistCstcAtOrBelowHoriz && !vdistCstcAtOrBelowVert) {
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
