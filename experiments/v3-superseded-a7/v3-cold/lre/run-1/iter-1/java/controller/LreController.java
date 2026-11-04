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
 * The Last Response Engine: the AUV's safety controller between the operator
 * controller and the autopilot (LRE-ARCH1, LRE-ARCH2). Each step it runs the
 * operations on the current sensor data, then evaluates the transitions of
 * the current mode.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;

    private final CalcVel velocityOp;
    private final CalcCStc closestStaticOp;
    private final CheckOPEZ opezOp;
    private final CalcCDyn closestDynamicOp;
    private final CalcCPA cpaOp;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velocityOp = new CalcVel(sensor);
        this.closestStaticOp = new CalcCStc(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.closestDynamicOp = new CalcCDyn(sensor);
        this.cpaOp = new CalcCPA(sensor);
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
        // --- Operations, before any guard is evaluated (LRE-OP1 .. LRE-OP5) ---
        velocityOp.compute();
        closestStaticOp.compute();
        opezOp.compute();
        closestDynamicOp.compute();
        cpaOp.compute();

        // --- Named boolean predicates ---
        boolean inOpez = opezOp.inOpez();
        boolean velWithinMomEntryLimit = velocityOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean dynamicObsBeyondMomEntryDist = sensor.odist(closestDynamicOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean staticObsBeyondMomEntryDist = sensor.odist(closestStaticOp.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean momEntryAllowed = velWithinMomEntryLimit && !inOpez
                && dynamicObsBeyondMomEntryDist && staticObsBeyondMomEntryDist;
        boolean cdaBelowMinSafeDist = cpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaOp.tcpa() >= LreConstants.MIN_TCPA;
        boolean collisionCourse = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean hvelAtOrAboveLimit = velocityOp.hvel() >= LreConstants.HIGH_HORIZ_VEL;
        boolean staticHdistWithinLimit = sensor.hdist(closestStaticOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vvelAtOrAboveLimit = velocityOp.vvel() >= LreConstants.HIGH_VERT_VEL;
        boolean staticVdistWithinLimit = sensor.vdist(closestStaticOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVdistWithinDfltLimit = sensor.vdist(closestStaticOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtOrAboveLimit && staticHdistWithinLimit;
        boolean vertApproach = vvelAtOrAboveLimit && staticVdistWithinLimit;
        boolean staticObsCleared = !staticHdistWithinLimit && !staticVdistWithinLimit;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            // OCM handles exactly reqVel, reqHdng and reqMOM (LRE-Beh19).
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.requestedVelocity()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.requestedHeading()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                // LRE-Beh4
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask && !inOpez) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (horizApproach && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (staticVdistWithinDfltLimit && !inOpez && !collisionCourse && !horizApproach && !vertApproach) {
                // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse
                    && !horizApproach && !vertApproach && !staticVdistWithinDfltLimit) {
                // LRE-Beh12
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
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
            } else if (staticObsCleared && !inOpez && !collisionCourse) {
                // LRE-Beh13
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafeDist) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
