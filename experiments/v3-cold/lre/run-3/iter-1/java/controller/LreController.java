package lre.controller;

import lre.LreConstants;
import lre.actuator.Actuator;
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
 * The Last Response Engine: the AUV's reactive safety controller between the
 * operator controller and the autopilot. It reads the Sensor, computes the
 * derived quantities each step, and advises velocity and heading through the
 * Actuator while switching between OCM, MOM, HCM and CAM.
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

    /** A controller in OCM reading the given sensor and advising the given actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor);
        this.calcCPA = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: refresh the derived quantities, then take at most one
     * transition of the current mode.
     *
     * @param event the operator command received in this cycle, or null if none
     */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        boolean inOpez = checkOPEZ.inOpez();
        boolean velWithinMomSpeed = calcVel.vel() <= LreConstants.MOM_SPEED;
        boolean dynObsBeyondClearance = sensor.odist(calcCDyn.cdyn()) > LreConstants.MOM_ENTRY_CLEARANCE;
        boolean staticObsBeyondClearance = sensor.odist(calcCStc.cstc()) > LreConstants.MOM_ENTRY_CLEARANCE;
        boolean momEntryAllowed = velWithinMomSpeed && !inOpez && dynObsBeyondClearance && staticObsBeyondClearance;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtApproachSpeed = calcVel.hvel() >= LreConstants.APPROACH_SPEED;
        boolean vvelAtApproachSpeed = calcVel.vvel() >= LreConstants.APPROACH_SPEED;
        boolean staticHdistWithinLimit = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVdistWithinLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVdistWithinDfltLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtApproachSpeed && staticHdistWithinLimit;
        boolean vertApproach = vvelAtApproachSpeed && staticVdistWithinLimit;

        if (currentMode == LreMode.OCM) {
            // Only reqVel, reqHdng and reqMOM are handled in OCM.
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velocityRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velocityRequest.velocity()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng headingRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(headingRequest.heading()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_SPEED));
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
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            } else if (horizApproach && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            }
            // Before the default-threshold branch: with equal thresholds that branch subsumes this one.
            else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            } else if (staticVdistWithinDfltLimit && !inOpez && !collisionCourse && !horizApproach && !vertApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (!staticHdistWithinLimit && !staticVdistWithinLimit && !inOpez && !collisionCourse) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_SPEED));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            }
        }
    }
}
