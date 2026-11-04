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
 * The Last Response Engine: mediates between the operator controller and the
 * autopilot of the AUV (LRE-ARCH1, LRE-ARCH2).
 *
 * <p>Each step runs the operations over the sensor data, evaluates the named
 * guard predicates and takes at most one transition of the current mode. The
 * variables of LRE-Var1..Var8 are held by the operations that compute them.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVelOp;
    private final CalcCStc calcCStcOp;
    private final CheckOPEZ checkOpezOp;
    private final CalcCDyn calcCDynOp;
    private final CalcCPA calcCpaOp;

    /** Creates the controller in OCM over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVelOp = new CalcVel(sensor);
        this.calcCStcOp = new CalcCStc(sensor);
        this.checkOpezOp = new CheckOPEZ(sensor);
        this.calcCDynOp = new CalcCDyn(sensor);
        this.calcCpaOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle.
     *
     * @param event the operator input received this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        // Operations, before any guard is evaluated (LRE-OP1..OP5).
        calcVelOp.compute();
        calcCStcOp.compute();
        checkOpezOp.compute();
        calcCDynOp.compute();
        calcCpaOp.compute();

        // --- Named guard predicates ---
        boolean inOpez = checkOpezOp.inOpez();
        boolean velAtMostOne = calcVelOp.vel() <= 1.0;
        boolean dynObsBeyondOne = sensor.odist(calcCDynOp.cdyn()) > 1.0;
        boolean staticObsBeyondOne = sensor.odist(calcCStcOp.cstc()) > 1.0;
        boolean momEntryAllowed = velAtMostOne && !inOpez && dynObsBeyondOne && staticObsBeyondOne;
        boolean cdaBelowMinSafe = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLeastOne = calcVelOp.hvel() >= 1.0;
        boolean vvelAtLeastOne = calcVelOp.vvel() >= 1.0;
        boolean staticHorizWithin = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertWithin = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticDfltVertWithin = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtLeastOne && staticHorizWithin;
        boolean vertVelApproach = vvelAtLeastOne && staticVertWithin;
        boolean staticObsClear = !staticHorizWithin && !staticVertWithin;

        // --- Mode-nested transitions, highest priority first ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2: pass the requested velocity through.
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3: pass the requested heading through.
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                // LRE-Beh4; MOM entry advises 1 m/s (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (collisionCourse) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (inOpez && !collisionCourse) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask && !collisionCourse && !inOpez) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM && !collisionCourse && !inOpez) {
                // LRE-Beh12; HCM entry advises 0 m/s (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (horizApproach && !collisionCourse && !inOpez) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertVelApproach && !horizApproach && !collisionCourse && !inOpez) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (staticDfltVertWithin && !vertVelApproach && !horizApproach && !collisionCourse && !inOpez) {
                // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15
                currentMode = LreMode.OCM;
            } else if (collisionCourse) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (inOpez && !collisionCourse) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (staticObsClear && !collisionCourse && !inOpez) {
                // LRE-Beh13; MOM entry advises 1 m/s (LRE-FR2).
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
