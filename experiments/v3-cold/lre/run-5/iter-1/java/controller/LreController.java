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
 * AUV's autopilot controller (LRE-ARCH1, LRE-ARCH2). Each step it recomputes
 * the derived quantities from the sensor, then evaluates the transitions of
 * the current mode.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVelOp;
    private final CalcCStc calcCStcOp;
    private final CalcCDyn calcCDynOp;
    private final CheckOPEZ checkOpezOp;
    private final CalcCPA calcCpaOp;

    /** Creates the controller in OCM over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVelOp = new CalcVel(sensor);
        this.calcCStcOp = new CalcCStc(sensor);
        this.calcCDynOp = new CalcCDyn(sensor);
        this.checkOpezOp = new CheckOPEZ(sensor);
        this.calcCpaOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the operator input received this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        // Operations, before the guards are evaluated (LRE-OP1..OP5).
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOpezOp.compute();
        calcCpaOp.compute();

        // Named guard predicates.
        boolean inOpez = checkOpezOp.inOpez();
        boolean velAtMostOne = calcVelOp.vel() <= 1.0;
        boolean cdynOdistAboveOne = sensor.odist(calcCDynOp.cdyn()) > 1.0;
        boolean cstcOdistAboveOne = sensor.odist(calcCStcOp.cstc()) > 1.0;
        boolean cdaBelowMinSafeDist = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean hvelAtLeastOne = calcVelOp.hvel() >= 1.0;
        boolean vvelAtLeastOne = calcVelOp.vvel() >= 1.0;
        boolean cstcHdistWithinHorizDist = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVdistWithinVertDist = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVdistWithinDfltVertDist = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean collisionCourse = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean horizontalCaution = hvelAtLeastOne && cstcHdistWithinHorizDist;
        boolean verticalCaution = vvelAtLeastOne && cstcVdistWithinVertDist;

        if (currentMode == LreMode.OCM) {
            // LRE-Beh19: OCM handles reqVel, reqHdng and reqMOM only.
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
            } else if (event instanceof InputEvent.ReqMOM
                    && velAtMostOne && !inOpez && cdynOdistAboveOne && cstcOdistAboveOne) {
                // LRE-Beh4; MOM entry (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (horizontalCaution && !inOpez && !collisionCourse) {
                // LRE-Beh9; HCM entry (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (verticalCaution && !inOpez && !collisionCourse && !horizontalCaution) {
                // LRE-Beh11; HCM entry (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (cstcVdistWithinDfltVertDist && !inOpez && !collisionCourse && !horizontalCaution
                    && !verticalCaution) {
                // LRE-Beh10; HCM entry (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionCourse && !horizontalCaution
                    && !verticalCaution && !cstcVdistWithinDfltVertDist) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse && !horizontalCaution
                    && !verticalCaution && !cstcVdistWithinDfltVertDist) {
                // LRE-Beh12; HCM entry (LRE-FR3)
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
            } else if (!cstcHdistWithinHorizDist && !cstcVdistWithinVertDist && !inOpez && !collisionCourse) {
                // LRE-Beh13; MOM entry (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafeDist) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
