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
 * The Last Response Engine: a reactive safety controller that mediates
 * between the operator controller and the autopilot controller of the AUV.
 *
 * <p>{@link #step(InputEvent)} is called once per control cycle. It refreshes
 * the derived quantities, evaluates every transition condition and, at most
 * once per cycle, changes mode and advises the autopilot.
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

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtMostOne = vel <= 1.0;
        boolean odistCDynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCStcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafeDist = cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCStcAtMostHorizDist = sensor.hdist(cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCStcAtMostVertDist = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCStcAtMostDfltVertDist = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel requested = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(requested.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng requested = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(requested.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtMostOne && !inOpez
                    && odistCDynAboveOne && odistCStcAboveOne) {
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
            } else if (hvelAtLeastOne && hdistCStcAtMostHorizDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCStcAtMostDfltVertDist) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtLeastOne && vdistCStcAtMostVertDist) {
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
            } else if (!hdistCStcAtMostHorizDist && !vdistCStcAtMostVertDist) {
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
