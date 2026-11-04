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
 * The Last Response Engine: the AUV's safety controller (LRE-ARCH1,
 * LRE-ARCH2). It sits between the operator controller and the autopilot,
 * reads the sensor each step, and advises velocity and heading through the
 * actuator.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velocityOp;
    private final CalcCStc staticOp;
    private final CalcCDyn dynamicOp;
    private final CheckOPEZ opezOp;
    private final CalcCPA cpaOp;

    /** Creates the controller in OCM over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velocityOp = new CalcVel(sensor);
        this.staticOp = new CalcCStc(sensor);
        this.dynamicOp = new CalcCDyn(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.cpaOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle: recomputes the derived quantities, then takes at
     * most one transition of the current mode.
     *
     * @param event the operator input received in this cycle, or {@code null}
     *              when there is none
     */
    public void step(InputEvent event) {
        velocityOp.compute();
        staticOp.compute();
        dynamicOp.compute();
        opezOp.compute();
        cpaOp.compute();

        boolean inOpez = opezOp.inOpez();
        boolean velWithinLimit = velocityOp.vel() <= LreConstants.VEL_THRESHOLD;
        boolean dynamicObsClear = sensor.odist(dynamicOp.cdyn()) > LreConstants.ENTRY_CLEARANCE_DIST;
        boolean staticObsClear = sensor.odist(staticOp.cstc()) > LreConstants.ENTRY_CLEARANCE_DIST;
        boolean momEntryAllowed = velWithinLimit && !inOpez && dynamicObsClear && staticObsClear;
        boolean cdaBelowMinSafe = cpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtThreshold = velocityOp.hvel() >= LreConstants.VEL_THRESHOLD;
        boolean vvelAtThreshold = velocityOp.vvel() >= LreConstants.VEL_THRESHOLD;
        boolean staticHorizNear = sensor.hdist(staticOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertNear = sensor.vdist(staticOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticDfltVertNear = sensor.vdist(staticOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtThreshold && staticHorizNear;
        boolean vertApproach = vvelAtThreshold && staticVertNear;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2: pass the requested velocity through.
                InputEvent.ReqVel requestedVel = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(requestedVel.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3: pass the requested heading through.
                InputEvent.ReqHdng requestedHdng = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(requestedHdng.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                // LRE-Beh4; entry to MOM (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VELOCITY));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            } else if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                // LRE-Beh12; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (horizApproach && !inOpez && !collisionCourse) {
                // LRE-Beh9; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                // LRE-Beh11; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (staticDfltVertNear && !inOpez && !collisionCourse && !horizApproach && !vertApproach) {
                // LRE-Beh10; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
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
            } else if (!staticHorizNear && !staticVertNear && !inOpez && !collisionCourse) {
                // LRE-Beh13; entry to MOM (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VELOCITY));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            }
        }
    }
}
