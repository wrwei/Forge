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
 * AUV's autopilot. Each cycle it computes the derived quantities from the
 * sensor, then decides from the current mode, the operator's input and those
 * quantities whether to change mode, advising velocity and heading to the
 * autopilot through the actuator.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CheckOPEZ checkOPEZ;
    private final CalcCDyn calcCDyn;
    private final CalcCPA calcCPA;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.calcCPA = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle. {@code event} is the operator's input this cycle, or
     * {@code null} if there is none.
     */
    public void step(InputEvent event) {
        // --- Operations: this cycle's derived quantities, before any guard ---
        calcVel.compute();
        calcCStc.compute();
        checkOPEZ.compute();
        calcCDyn.compute();
        calcCPA.compute();

        // --- Named boolean predicates ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtOrBelowOne = calcVel.vel() <= 1.0;
        boolean dynamicDistAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean staticDistAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean momEntryClear = velAtOrBelowOne && !inOpez && dynamicDistAboveOne && staticDistAboveOne;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveOne = calcVel.hvel() >= 1.0;
        boolean vvelAtOrAboveOne = calcVel.vvel() >= 1.0;
        boolean staticHorizWithinLimit = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertWithinLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVertWithinDefault = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtOrAboveOne && staticHorizWithinLimit;
        boolean vertApproach = vvelAtOrAboveOne && staticVertWithinLimit;

        // --- Two-level if-else: one block per mode; operator inputs first,
        //     then autonomous transitions in safety order ---
        if (currentMode == LreMode.OCM) {
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
            } else if (horizApproach && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (staticVertWithinDefault && !inOpez && !collisionCourse && !horizApproach
                    && !vertApproach) {
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
            } else if (!staticHorizWithinLimit && !staticVertWithinLimit && !inOpez && !collisionCourse) {
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
