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
 * The Last Response Engine safety controller (LRE-ARCH1, LRE-ARCH2): a
 * single-method, mode-nested if-else state machine over the four
 * operating modes. Each step recomputes the derived quantities via the
 * operations, then evaluates the transition guards.
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

    /** The controller's current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates one control cycle: recomputes derived quantities, then
     * evaluates event-triggered and autonomous transitions.
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
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean collisionImminent = cda < LreConstants.minSafeDist && tcpa >= 0.0;
        boolean hvelAtOrAboveOne = hvel >= 1.0;
        boolean vvelAtOrAboveOne = vvel >= 1.0;
        boolean hdistCstcAtOrBelowHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtOrBelowVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtOrBelowDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel requested = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(requested.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng requested = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(requested.value()));
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
            } else if (event instanceof InputEvent.tick && !inOpez && collisionImminent) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.tick && !inOpez && !collisionImminent
                    && hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.tick && !inOpez && !collisionImminent
                    && vdistCstcAtOrBelowDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.tick && !inOpez && !collisionImminent
                    && vvelAtOrAboveOne && vdistCstcAtOrBelowVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && inOpez) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && !inOpez && collisionImminent) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.tick && !inOpez && !collisionImminent
                    && !hdistCstcAtOrBelowHoriz && !vdistCstcAtOrBelowVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.tick && !cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
