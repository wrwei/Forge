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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2): reads the sensor, computes
 * the derived quantities with its operations, and decides the mode and the
 * advice sent to the autopilot through the actuator. Starts in OCM.
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

    /** A controller reading {@code sensor} and advising through {@code actuator}. */
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
     * One control cycle: recompute the derived quantities, then take at most
     * one transition of the current mode, for the given operator event.
     */
    public void step(InputEvent event) {
        // --- Operations, before any guard is evaluated (LRE-OP1..OP5) ---
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOpezOp.compute();
        calcCpaOp.compute();

        // --- Named guard predicates ---
        boolean inOpez = checkOpezOp.inOpez();
        boolean velWithinMomLimit = calcVelOp.vel() <= LreConstants.momEntryMaxVel;
        boolean cdynDistClear = sensor.odist(calcCDynOp.cdyn()) > LreConstants.momEntryMinDist;
        boolean cstcDistClear = sensor.odist(calcCStcOp.cstc()) > LreConstants.momEntryMinDist;
        boolean momEntryAllowed = velWithinMomLimit && !inOpez && cdynDistClear && cstcDistClear;
        boolean cdaBelowMinSafe = calcCpaOp.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= LreConstants.minTcpa;
        boolean collisionRisk = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveLimit = calcVelOp.hvel() >= LreConstants.highVel;
        boolean hdistWithinHoriz = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.staticObsHorizDist;
        boolean horizApproach = hvelAtOrAboveLimit && hdistWithinHoriz;
        boolean vvelAtOrAboveLimit = calcVelOp.vvel() >= LreConstants.highVel;
        boolean vdistWithinVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.staticObsVertDist;
        boolean vertApproach = vvelAtOrAboveLimit && vdistWithinVert;
        boolean vdistWithinDfltVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean staticObstacleClear = !hdistWithinHoriz && !vdistWithinVert;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                // LRE-Beh2: pass the requested velocity through
                InputEvent.reqVel velocityRequest = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(velocityRequest.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                // LRE-Beh3: pass the requested heading through
                InputEvent.reqHdng headingRequest = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(headingRequest.value()));
            } else if (event instanceof InputEvent.reqMOM && momEntryAllowed) {
                // LRE-Beh4, entering MOM (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(LreConstants.momVelocity));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(LreConstants.stopVelocity));
            } else if (event instanceof InputEvent.reqHCM) {
                // LRE-Beh12, entering HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(LreConstants.hcmVelocity));
            } else if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (horizApproach && !inOpez && !collisionRisk) {
                // LRE-Beh9, entering HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(LreConstants.hcmVelocity));
            } else if (vertApproach && !inOpez && !collisionRisk && !horizApproach) {
                // LRE-Beh11, entering HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(LreConstants.hcmVelocity));
            } else if (vdistWithinDfltVert && !inOpez && !collisionRisk && !horizApproach && !vertApproach) {
                // LRE-Beh10, entering HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(LreConstants.hcmVelocity));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh15
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (staticObstacleClear && !inOpez && !collisionRisk) {
                // LRE-Beh13, entering MOM (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(LreConstants.momVelocity));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(LreConstants.stopVelocity));
            }
        }
    }
}
