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
 * The Last Response Engine: the AUV's safety controller, between the operator controller
 * and the autopilot (LRE-ARCH1, LRE-ARCH2). Each step it runs its operations over the
 * sensor data, then takes at most one mode transition.
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

    /** Creates the controller, in OCM, over the given sensor and actuator. */
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
     * One control cycle.
     *
     * @param event the operator input received this cycle, or {@code null} when there is none;
     *              autonomous transitions are evaluated in either case
     */
    public void step(InputEvent event) {
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOPEZOp.compute();
        calcCPAOp.compute();

        boolean inOpez = checkOPEZOp.inOpez();
        boolean velAtMostNormal = calcVelOp.vel() <= 1.0;
        boolean dynObsBeyondUnitDist = sensor.odist(calcCDynOp.cdyn()) > 1.0;
        boolean staticObsBeyondUnitDist = sensor.odist(calcCStcOp.cstc()) > 1.0;
        boolean momEntryClear = velAtMostNormal && !inOpez && dynObsBeyondUnitDist && staticObsBeyondUnitDist;
        boolean cdaBelowMinSafe = calcCPAOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPAOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLeastNormal = calcVelOp.hvel() >= 1.0;
        boolean vvelAtLeastNormal = calcVelOp.vvel() >= 1.0;
        boolean staticHdistWithinLimit = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVdistWithinLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVdistWithinDflt = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproachStatic = hvelAtLeastNormal && staticHdistWithinLimit;
        boolean vertApproachStatic = vvelAtLeastNormal && staticVdistWithinLimit;
        boolean staticObsClear = !staticHdistWithinLimit && !staticVdistWithinLimit;

        if (currentMode == LreMode.OCM) {
            // LRE-Beh19: OCM handles reqVel, reqHdng and reqMOM only.
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryClear) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // Operator commands first; then the autonomous transitions, safest first.
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (horizApproachStatic && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertApproachStatic && !inOpez && !collisionCourse && !horizApproachStatic) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (staticVdistWithinDflt && !inOpez && !collisionCourse && !horizApproachStatic
                    && !vertApproachStatic) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (staticObsClear && !inOpez && !collisionCourse) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
