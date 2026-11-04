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
 * The Last Response Engine: mediates between the operator controller and the autopilot
 * controller of the AUV (LRE-ARCH1, LRE-ARCH2).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velCalc;
    private final CalcCStc staticCalc;
    private final CalcCDyn dynamicCalc;
    private final CheckOPEZ opezCheck;
    private final CalcCPA cpaCalc;

    /** An LRE in OCM reading the given sensor and advising through the given actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velCalc = new CalcVel(sensor);
        this.staticCalc = new CalcCStc(sensor);
        this.dynamicCalc = new CalcCDyn(sensor);
        this.opezCheck = new CheckOPEZ(sensor);
        this.cpaCalc = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: recompute the derived quantities, then take at most one transition.
     * The event is the operator input of this cycle, or null when there is none.
     */
    public void step(InputEvent event) {
        // --- Operations, before any guard is evaluated (LRE-OP1 to LRE-OP5) ---
        velCalc.compute();
        staticCalc.compute();
        dynamicCalc.compute();
        opezCheck.compute();
        cpaCalc.compute();

        // --- Named predicates ---
        // LRE-GP1
        boolean inOpez = opezCheck.inOpez();
        // LRE-Beh4
        boolean velWithinMomEntryLimit = velCalc.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean cdynBeyondMomEntryDist = sensor.odist(dynamicCalc.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcBeyondMomEntryDist = sensor.odist(staticCalc.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean momEntrySafe = velWithinMomEntryLimit && !inOpez
                && cdynBeyondMomEntryDist && cstcBeyondMomEntryDist;
        // LRE-Beh8, LRE-Beh14, LRE-Beh18
        boolean cdaBelowMinSafeDist = cpaCalc.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaCalc.tcpa() >= LreConstants.MIN_TCPA;
        boolean collisionCourse = cdaBelowMinSafeDist && tcpaNonNegative;
        // LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh13
        boolean hvelAtOrAboveLimit = velCalc.hvel() >= LreConstants.HORIZ_VEL_LIMIT;
        boolean vvelAtOrAboveLimit = velCalc.vvel() >= LreConstants.VERT_VEL_LIMIT;
        boolean cstcHdistWithinLimit = sensor.hdist(staticCalc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVdistWithinLimit = sensor.vdist(staticCalc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVdistWithinDfltLimit = sensor.vdist(staticCalc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean fastHorizApproach = hvelAtOrAboveLimit && cstcHdistWithinLimit;
        boolean fastVertApproach = vvelAtOrAboveLimit && cstcVdistWithinLimit;
        boolean cstcClear = !cstcHdistWithinLimit && !cstcVdistWithinLimit;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh19: OCM handles reqVel, reqHdng and reqMOM only.
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel request = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(request.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng request = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(request.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntrySafe) {
                // LRE-Beh4
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
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
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADVISED_VEL));
            } else if (fastHorizApproach && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (fastVertApproach && !inOpez && !collisionCourse && !fastHorizApproach) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (cstcVdistWithinDfltLimit && !inOpez && !collisionCourse
                    && !fastHorizApproach && !fastVertApproach) {
                // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse
                    && !fastHorizApproach && !fastVertApproach && !cstcVdistWithinDfltLimit) {
                // LRE-Beh12
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
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (cstcClear && !inOpez && !collisionCourse) {
                // LRE-Beh13
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
