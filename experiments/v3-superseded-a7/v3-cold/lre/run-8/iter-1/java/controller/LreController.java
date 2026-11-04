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
 * AUV's autopilot, choosing an operating mode from operator requests and the
 * quantities its operations derive from the sensor each step.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velOp;
    private final CalcCStc cstcOp;
    private final CheckOPEZ opezOp;
    private final CalcCDyn cdynOp;
    private final CalcCPA cpaOp;

    /** Creates the controller, in OCM, over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velOp = new CalcVel(sensor);
        this.cstcOp = new CalcCStc(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.cdynOp = new CalcCDyn(sensor);
        this.cpaOp = new CalcCPA(sensor);
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
        // --- Operations, before any guard is evaluated ---
        velOp.compute();
        cstcOp.compute();
        opezOp.compute();
        cdynOp.compute();
        cpaOp.compute();

        // --- Named boolean predicates ---
        boolean inOpez = opezOp.inOpez();
        boolean velAtOrBelowMomEntry = velOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean cdynOdistAboveMomEntry = sensor.odist(cdynOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcOdistAboveMomEntry = sensor.odist(cstcOp.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cdaBelowMinSafeDist = cpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaOp.tcpa() >= 0.0;
        boolean hvelAtOrAboveCaution = velOp.hvel() >= LreConstants.HORIZ_CAUTION_VEL;
        boolean vvelAtOrAboveCaution = velOp.vvel() >= LreConstants.VERT_CAUTION_VEL;
        boolean cstcHdistWithinHoriz = sensor.hdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVdistWithinVert = sensor.vdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVdistWithinDfltVert = sensor.vdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean momEntrySafe = velAtOrBelowMomEntry && !inOpez && cdynOdistAboveMomEntry && cstcOdistAboveMomEntry;
        boolean collisionCourse = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean horizApproach = hvelAtOrAboveCaution && cstcHdistWithinHoriz;
        boolean vertApproach = vvelAtOrAboveCaution && cstcVdistWithinVert;
        boolean staticObsClear = !cstcHdistWithinHoriz && !cstcVdistWithinVert;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel reqVelEvent = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(reqVelEvent.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng reqHdngEvent = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(reqHdngEvent.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntrySafe) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionCourse) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADVISED_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (horizApproach && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (cstcVdistWithinDfltVert && !inOpez && !collisionCourse && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach && !cstcVdistWithinDfltVert) {
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
            } else if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADVISED_VEL));
            }
        }
    }
}
