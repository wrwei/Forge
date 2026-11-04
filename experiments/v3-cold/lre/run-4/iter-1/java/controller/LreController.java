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
 * The Last Response Engine: mediates between the operator controller and
 * the AUV autopilot (LRE-ARCH1, LRE-ARCH2).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velCalc;
    private final CalcCStc cstcCalc;
    private final CheckOPEZ opezCheck;
    private final CalcCDyn cdynCalc;
    private final CalcCPA cpaCalc;

    /** Creates the controller in OCM over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velCalc = new CalcVel(sensor);
        this.cstcCalc = new CalcCStc(sensor);
        this.opezCheck = new CheckOPEZ(sensor);
        this.cdynCalc = new CalcCDyn(sensor);
        this.cpaCalc = new CalcCPA(sensor);
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
        // Operations, before any guard is evaluated (LRE-OP1..5).
        velCalc.compute();
        cstcCalc.compute();
        opezCheck.compute();
        cdynCalc.compute();
        cpaCalc.compute();

        // --- Named guard predicates ---
        boolean inOpez = opezCheck.inOpez();
        boolean velWithinEntryLimit = velCalc.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean cdynBeyondEntryDist = sensor.odist(cdynCalc.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcBeyondEntryDist = sensor.odist(cstcCalc.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean momEntryAllowed = velWithinEntryLimit && !inOpez && cdynBeyondEntryDist && cstcBeyondEntryDist;
        boolean cdaBelowMinSafe = cpaCalc.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaCalc.tcpa() >= LreConstants.MIN_TCPA;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveLimit = velCalc.hvel() >= LreConstants.HIGH_HORIZ_VEL;
        boolean cstcHorizWithinThreshold = sensor.hdist(cstcCalc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean fastHorizApproach = hvelAtOrAboveLimit && cstcHorizWithinThreshold;
        boolean vvelAtOrAboveLimit = velCalc.vvel() >= LreConstants.HIGH_VERT_VEL;
        boolean cstcVertWithinThreshold = sensor.vdist(cstcCalc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean fastVertApproach = vvelAtOrAboveLimit && cstcVertWithinThreshold;
        boolean cstcVertWithinDfltThreshold = sensor.vdist(cstcCalc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean staticObsCleared = !cstcHorizWithinThreshold && !cstcVertWithinThreshold;

        // --- Modes ---
        if (currentMode == LreMode.OCM) {
            // OCM handles exactly reqVel, reqHdng and reqMOM (LRE-Beh19).
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
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionCourse) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                // LRE-Beh12
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (fastHorizApproach && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (fastVertApproach && !inOpez && !collisionCourse && !fastHorizApproach) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (cstcVertWithinDfltThreshold && !inOpez && !collisionCourse && !fastHorizApproach
                    && !fastVertApproach) {
                // LRE-Beh10
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
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
