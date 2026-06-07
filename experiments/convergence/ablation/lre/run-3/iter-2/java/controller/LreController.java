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
 * The Last Response Engine: a mode-nested if-else state machine mediating
 * between the operator controller and the autopilot controller of the AUV.
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
        this.checkOPEZ = new CheckOPEZ(sensor, calcCStc);
        this.calcCPA = new CalcCPA(sensor, calcCDyn);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all transitions, event-triggered and autonomous. Called once
     * per control cycle.
     */
    public void step(InputEvent event) {
        // Operations computed before evaluating transition guards each step.
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

        // --- Named boolean predicates ---
        boolean velAtMostOne = this.vel <= 1.0;
        boolean odistCdynAboveMinSafe = sensor.odist(this.cdyn) > LreConstants.MIN_SAFE_DIST;
        boolean odistCstcAboveMinSafe = sensor.odist(this.cstc) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowMinSafe = this.cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = this.tcpa >= 0.0;
        boolean hvelAtLeastOne = this.hvel >= 1.0;
        boolean vvelAtLeastOne = this.vvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(this.cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAtMostVert = sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;

        // --- Mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(rv.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(rh.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtMostOne && !inOpez
                    && odistCdynAboveMinSafe && odistCstcAboveMinSafe) {
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
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hvelAtLeastOne && hdistCstcAtMostHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtMostDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtLeastOne && vdistCstcAtMostVert) {
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
            } else if (!hdistCstcAtMostHoriz && !vdistCstcAtMostVert) {
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
