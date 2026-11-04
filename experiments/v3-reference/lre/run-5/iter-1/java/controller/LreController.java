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
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CheckOPEZ checkOpez;
    private final CalcCDyn calcCDyn;
    private final CalcCPA calcCpa;

    /** Creates the LRE, in OCM, reading {@code sensor} and advising through {@code actuator}. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.checkOpez = new CheckOPEZ(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.calcCpa = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: recomputes the derived quantities, then takes at most
     * one transition of the current mode.
     *
     * @param event the operator input received this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        // Derived quantities, before any guard is evaluated (LRE-OP1..OP5).
        calcVel.compute();
        calcCStc.compute();
        checkOpez.compute();
        calcCDyn.compute();
        calcCpa.compute();

        // --- Named predicates ---
        boolean inOpez = checkOpez.inOpez();
        boolean velAtMostOne = calcVel.vel() <= 1.0;
        boolean cdynOdistAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean cstcOdistAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean momEntryClear = velAtMostOne && !inOpez && cdynOdistAboveOne && cstcOdistAboveOne;
        boolean cdaBelowMinSafe = calcCpa.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpa.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLeastOne = calcVel.hvel() >= 1.0;
        boolean vvelAtLeastOne = calcVel.vvel() >= 1.0;
        boolean cstcHdistWithinHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVdistWithinVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVdistWithinDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproachFast = hvelAtLeastOne && cstcHdistWithinHoriz;
        boolean vertApproachFast = vvelAtLeastOne && cstcVdistWithinVert;
        boolean cstcClear = !cstcHdistWithinHoriz && !cstcVdistWithinVert;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh19: OCM handles only reqVel, reqHdng and reqMOM.
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryClear) {
                // LRE-Beh4; entering MOM advises 1 m/s (LRE-FR2)
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
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionCourse) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                // LRE-Beh12; entering HCM advises 0 m/s (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (horizApproachFast && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertApproachFast && !inOpez && !collisionCourse && !horizApproachFast) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (cstcVdistWithinDfltVert && !inOpez && !collisionCourse && !horizApproachFast
                    && !vertApproachFast) {
                // LRE-Beh10
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
            } else if (cstcClear && !inOpez && !collisionCourse) {
                // LRE-Beh13; entering MOM advises 1 m/s (LRE-FR2)
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
