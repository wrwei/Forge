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
 * autopilot of the AUV. Each step it computes the derived quantities from the
 * sensor, then takes at most one transition of its mode machine.
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
    public LreMode currentMode() { return currentMode; }

    /** One control cycle; {@code event} is null when no operator input arrived. */
    public void step(InputEvent event) {
        // --- Operations, run before any guard is evaluated ---
        calcVelOp.compute();
        calcCStcOp.compute();
        checkOpezOp.compute();
        calcCDynOp.compute();
        calcCpaOp.compute();

        // --- Named boolean predicates ---
        boolean inOpez = checkOpezOp.inOpez();
        boolean velWithinMomEntry = calcVelOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean dynObsBeyondEntryDist = sensor.odist(calcCDynOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean staticObsBeyondEntryDist = sensor.odist(calcCStcOp.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean momEntryAllowed = velWithinMomEntry && !inOpez && dynObsBeyondEntryDist && staticObsBeyondEntryDist;
        boolean cdaBelowMinSafe = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveCaution = calcVelOp.hvel() >= LreConstants.CAUTION_VEL;
        boolean hdistStaticWithinLimit = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean horizApproach = hvelAtOrAboveCaution && hdistStaticWithinLimit;
        boolean vvelAtOrAboveCaution = calcVelOp.vvel() >= LreConstants.CAUTION_VEL;
        boolean vdistStaticWithinLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vertApproach = vvelAtOrAboveCaution && vdistStaticWithinLimit;
        boolean vdistStaticWithinDflt = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean staticObsClear = !hdistStaticWithinLimit && !vdistStaticWithinLimit;

        // --- Mode machine: one block per mode, transitions in priority order ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.requestedVelocity()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.requestedHeading()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (horizApproach && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (vdistStaticWithinDflt && !inOpez && !collisionCourse && !horizApproach && !vertApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse && !horizApproach
                    && !vertApproach && !vdistStaticWithinDflt) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
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
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
