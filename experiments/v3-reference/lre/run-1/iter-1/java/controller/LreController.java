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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2): mediates between the
 * operator controller and the AUV's autopilot. Each step it runs its
 * operations on the sensor data, then takes at most one mode transition and
 * issues the advice that transition calls for.
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

    /** A controller in OCM reading the given sensor and advising through the given actuator. */
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

        // Named guard predicates.
        boolean inOpez = checkOpezOp.inOpez();
        boolean velWithinLimit = calcVelOp.vel() <= LreConstants.VEL_LIMIT;
        boolean cdynBeyondClearance = sensor.odist(calcCDynOp.cdyn()) > LreConstants.OBS_CLEARANCE;
        boolean cstcBeyondClearance = sensor.odist(calcCStcOp.cstc()) > LreConstants.OBS_CLEARANCE;
        boolean momEntryAllowed = velWithinLimit && !inOpez && cdynBeyondClearance && cstcBeyondClearance;
        boolean cdaBelowMinSafe = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= LreConstants.MIN_TCPA;
        boolean collisionRisk = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLimit = calcVelOp.hvel() >= LreConstants.VEL_LIMIT;
        boolean vvelAtLimit = calcVelOp.vvel() >= LreConstants.VEL_LIMIT;
        boolean hdistWithinHoriz = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistWithinVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistWithinDfltVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtLimit && hdistWithinHoriz;
        boolean vertApproach = vvelAtLimit && vdistWithinVert;
        boolean staticObsClear = !hdistWithinHoriz && !vdistWithinVert;

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
                // LRE-Beh4; entering MOM advises normal speed (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                // LRE-Beh7.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (inOpez) {
                // LRE-Beh5.
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                // LRE-Beh8.
                currentMode = LreMode.CAM;
            } else if (horizApproach && !inOpez && !collisionRisk) {
                // LRE-Beh9; entering HCM advises reduced velocity (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (vertApproach && !inOpez && !collisionRisk && !horizApproach) {
                // LRE-Beh11.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (vdistWithinDfltVert && !inOpez && !collisionRisk && !horizApproach && !vertApproach) {
                // LRE-Beh10.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionRisk && !horizApproach
                    && !vertApproach && !vdistWithinDfltVert) {
                // LRE-Beh12.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15.
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh16.
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                // LRE-Beh14.
                currentMode = LreMode.CAM;
            } else if (staticObsClear && !inOpez && !collisionRisk) {
                // LRE-Beh13; entering MOM advises normal speed (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17.
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
