package lre.controller;

import lre.LreConstants;
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
 * The Last Response Engine: mediates between the operator controller and the
 * AUV's autopilot (LRE-ARCH1, LRE-ARCH2). Each step it recomputes the derived
 * quantities, then takes at most one transition of the current mode.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVelOp;
    private final CalcCStc calcCStcOp;
    private final CalcCDyn calcCDynOp;
    private final CheckOPEZ checkOPEZOp;
    private final CalcCPA calcCPAOp;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVelOp = new CalcVel(sensor);
        this.calcCStcOp = new CalcCStc(sensor);
        this.calcCDynOp = new CalcCDyn(sensor);
        this.checkOPEZOp = new CheckOPEZ(sensor);
        this.calcCPAOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the operator input received this cycle, or {@code null}
     */
    public void step(InputEvent event) {
        // --- Operations, before any guard is evaluated (LRE-OP1..OP5) ---
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOPEZOp.compute();
        calcCPAOp.compute();

        // --- Named boolean predicates ---
        boolean inOpez = checkOPEZOp.inOpez();
        boolean velAtOrBelowOne = calcVelOp.vel() <= 1.0;
        boolean odistDynAboveOne = sensor.odist(calcCDynOp.cdyn()) > 1.0;
        boolean odistStcAboveOne = sensor.odist(calcCStcOp.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCPAOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPAOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveOne = calcVelOp.hvel() >= 1.0;
        boolean vvelAtOrAboveOne = calcVelOp.vvel() >= 1.0;
        boolean hdistStcWithinHorizLimit = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistStcWithinVertLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistStcWithinDfltVertLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproachFast = hvelAtOrAboveOne && hdistStcWithinHorizLimit;
        boolean vertApproachFast = vvelAtOrAboveOne && vdistStcWithinVertLimit;

        // --- Mode-nested transitions, highest priority first ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel velocityRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velocityRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng headingRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(headingRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtOrBelowOne && !inOpez
                    && odistDynAboveOne && odistStcAboveOne) {
                // LRE-Beh4
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (horizApproachFast && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertApproachFast && !inOpez && !collisionCourse && !horizApproachFast) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vdistStcWithinDfltVertLimit && !inOpez && !collisionCourse && !horizApproachFast
                    && !vertApproachFast) {
                // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse && !horizApproachFast
                    && !vertApproachFast && !vdistStcWithinDfltVertLimit) {
                // LRE-Beh12
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (!hdistStcWithinHorizLimit && !vdistStcWithinVertLimit && !inOpez && !collisionCourse) {
                // LRE-Beh13
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
