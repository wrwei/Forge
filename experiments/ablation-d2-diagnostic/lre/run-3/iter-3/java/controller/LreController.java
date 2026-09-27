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
        this.checkOPEZ = new CheckOPEZ(sensor);
        this.calcCPA = new CalcCPA(sensor);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        calcVel.compute();
        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        calcCStc.compute();
        this.cstc = calcCStc.cstc();
        calcCDyn.compute();
        this.cdyn = calcCDyn.cdyn();
        checkOPEZ.compute();
        this.inOpez = checkOPEZ.inOpez();
        calcCPA.compute();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        // --- Named boolean predicates ---
        boolean velAtMostOne = vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafeDist = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCstcAtMostHorizLimit = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostVertLimit = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtMostDfltVertLimit =
                sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean collisionImminent = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean nearStaticHoriz = hvelAtLeastOne && hdistCstcAtMostHorizLimit;
        boolean nearStaticVert = vvelAtLeastOne && vdistCstcAtMostVertLimit;
        boolean clearOfStatic = !hdistCstcAtMostHorizLimit && !vdistCstcAtMostVertLimit;
        boolean momEntryClear =
                velAtMostOne && !inOpez && odistCdynAboveOne && odistCstcAboveOne;

        // --- Mode-nested if-else state machine ---
        // Within each mode the triggerless (autonomous) branches come first, each
        // negating the preceding triggerless guards, and the unconditional
        // operator-request branches come last, so every mode keeps a transition
        // that is enabled on its mode alone.
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                actuator.apply(new OutputEvent.advVel(rv.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                actuator.apply(new OutputEvent.advHdng(rh.value()));
            } else if (event instanceof InputEvent.reqMOM && momEntryClear) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (!inOpez && collisionImminent) {
                currentMode = LreMode.CAM;
            } else if (!inOpez && !collisionImminent && nearStaticHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (!inOpez && !collisionImminent && !nearStaticHoriz
                    && vdistCstcAtMostDfltVertLimit) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (!inOpez && !collisionImminent && !nearStaticHoriz
                    && !vdistCstcAtMostDfltVertLimit && nearStaticVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (!inOpez && collisionImminent) {
                currentMode = LreMode.CAM;
            } else if (!inOpez && !collisionImminent && clearOfStatic) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            }

        } else if (currentMode == LreMode.CAM) {
            if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            }
        }
    }
}
