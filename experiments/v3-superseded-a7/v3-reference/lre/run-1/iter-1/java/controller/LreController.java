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
 * AUV's autopilot (LRE-ARCH1, LRE-ARCH2).
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

    /** A controller reading {@code sensor} and advising through {@code actuator}. */
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
     * One control cycle: recompute the derived quantities, then take at most
     * one transition of the current mode.
     *
     * @param event the operator input received this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        // Derived quantities, recomputed before the guards are evaluated (LRE-OP1..OP5).
        calcVelOp.compute();
        calcCStcOp.compute();
        checkOpezOp.compute();
        calcCDynOp.compute();
        calcCpaOp.compute();

        // --- Named guard predicates ---
        boolean inOpez = checkOpezOp.inOpez();
        boolean velWithinMomEntryLimit = calcVelOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean dynObsBeyondEntryDist = sensor.odist(calcCDynOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_OBS_DIST;
        boolean staticObsBeyondEntryDist = sensor.odist(calcCStcOp.cstc()) > LreConstants.MOM_ENTRY_MIN_OBS_DIST;
        boolean cdaBelowMinSafeDist = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionRisk = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean hvelAtOrAboveLimit = calcVelOp.hvel() >= LreConstants.HCM_HORIZ_VEL_LIMIT;
        boolean staticHdistWithinLimit = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vvelAtOrAboveLimit = calcVelOp.vvel() >= LreConstants.HCM_VERT_VEL_LIMIT;
        boolean staticVdistWithinLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVdistWithinDfltLimit =
                sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean fastHorizNearStatic = hvelAtOrAboveLimit && staticHdistWithinLimit;
        boolean fastVertNearStatic = vvelAtOrAboveLimit && staticVdistWithinLimit;

        // --- Mode-nested transitions ---
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
            } else if (event instanceof InputEvent.ReqMOM && velWithinMomEntryLimit && !inOpez
                    && dynObsBeyondEntryDist && staticObsBeyondEntryDist) {
                // LRE-Beh4; entry to MOM (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADVISED_VEL));
            } else if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (fastHorizNearStatic && !inOpez && !collisionRisk) {
                // LRE-Beh9; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (fastVertNearStatic && !inOpez && !collisionRisk && !fastHorizNearStatic) {
                // LRE-Beh11; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (staticVdistWithinDfltLimit && !inOpez && !collisionRisk && !fastHorizNearStatic
                    && !fastVertNearStatic) {
                // LRE-Beh10; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionRisk && !fastHorizNearStatic
                    && !fastVertNearStatic && !staticVdistWithinDfltLimit) {
                // LRE-Beh12; entry to HCM (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (!staticHdistWithinLimit && !staticVdistWithinLimit && !inOpez && !collisionRisk) {
                // LRE-Beh13; entry to MOM (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafeDist) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADVISED_VEL));
            }
        }
    }
}
