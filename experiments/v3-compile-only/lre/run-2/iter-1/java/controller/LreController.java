package lre.controller;

import lre.actuator.Actuator;
import lre.event.InputEvent;
import lre.event.OutputEvent;
import lre.operation.CalcCDyn;
import lre.operation.CalcCPA;
import lre.operation.CalcCStc;
import lre.operation.CalcVel;
import lre.operation.CheckOPEZ;
import lre.sensor.Sensor;

/**
 * The Last Response Engine: the safety controller between the operator
 * controller and the AUV's autopilot. Each control cycle it recomputes the
 * derived quantities from the sensor, then takes at most one mode transition.
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

    /**
     * Creates the controller in OCM.
     *
     * @param sensor   the AUV's sensor
     * @param actuator the actuator that receives advice for the autopilot
     */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor);
        this.calcCPA = new CalcCPA(sensor);
    }

    /**
     * The current operating mode.
     *
     * @return the mode
     */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the operator input received this cycle, or null when there is none
     */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtOrBelowMomVel = calcVel.vel() <= LreConstants.MOM_VEL;
        boolean cdynBeyondMinSafeDist = sensor.odist(calcCDyn.cdyn()) > LreConstants.MIN_SAFE_DIST;
        boolean cstcBeyondMinSafeDist = sensor.odist(calcCStc.cstc()) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowMinSafeDist = calcCPA.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPA.tcpa() >= LreConstants.MIN_TCPA;
        boolean collisionCourse = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean hvelAtOrAboveMomVel = calcVel.hvel() >= LreConstants.MOM_VEL;
        boolean vvelAtOrAboveMomVel = calcVel.vvel() >= LreConstants.MOM_VEL;
        boolean cstcHorizWithinLimit = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVertWithinLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVertWithinDfltLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizVelHazard = hvelAtOrAboveMomVel && cstcHorizWithinLimit;
        boolean vertVelHazard = vvelAtOrAboveMomVel && cstcVertWithinLimit;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velocityRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.send(new OutputEvent.AdvVel(velocityRequest.velocity()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng headingRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.send(new OutputEvent.AdvHdng(headingRequest.heading()));
            } else if (event instanceof InputEvent.ReqMOM && velAtOrBelowMomVel && !inOpez
                    && cdynBeyondMinSafeDist && cstcBeyondMinSafeDist) {
                currentMode = LreMode.MOM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.MOM_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionCourse) {
                currentMode = LreMode.OCM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (horizVelHazard && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (vertVelHazard && !inOpez && !collisionCourse && !horizVelHazard) {
                currentMode = LreMode.HCM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (cstcVertWithinDfltLimit && !inOpez && !collisionCourse && !horizVelHazard
                    && !vertVelHazard) {
                currentMode = LreMode.HCM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (!cstcHorizWithinLimit && !cstcVertWithinLimit && !inOpez && !collisionCourse) {
                currentMode = LreMode.MOM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.MOM_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.send(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
