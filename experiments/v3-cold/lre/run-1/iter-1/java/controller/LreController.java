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

/** The Last Response Engine, the AUV's reactive safety controller (LRE-ARCH1, LRE-ARCH2). */
public final class LreController {
    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velocityOp;
    private final CalcCStc staticObsOp;
    private final CalcCDyn dynamicObsOp;
    private final CheckOPEZ opezOp;
    private final CalcCPA cpaOp;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velocityOp = new CalcVel(sensor);
        this.staticObsOp = new CalcCStc(sensor);
        this.dynamicObsOp = new CalcCDyn(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.cpaOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle.
     *
     * @param event the operator input received this cycle, or {@code null} when there is none
     */
    public void step(InputEvent event) {
        // Derived quantities, recomputed before the guards are evaluated (LRE-OP1..OP5).
        velocityOp.compute();
        staticObsOp.compute();
        dynamicObsOp.compute();
        opezOp.compute();
        cpaOp.compute();

        // --- Named guard predicates ---
        boolean inOpez = opezOp.inOpez();
        boolean velWithinMomEntryLimit = velocityOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean cdynBeyondMomEntryDist = sensor.odist(dynamicObsOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcBeyondMomEntryDist = sensor.odist(staticObsOp.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cdaBelowMinSafeDist = cpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean hvelAtOrAboveLimit = velocityOp.hvel() >= LreConstants.HORIZ_VEL_LIMIT;
        boolean vvelAtOrAboveLimit = velocityOp.vvel() >= LreConstants.VERT_VEL_LIMIT;
        boolean cstcHdistWithinHorizDist = sensor.hdist(staticObsOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVdistWithinVertDist = sensor.vdist(staticObsOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVdistWithinDfltVertDist = sensor.vdist(staticObsOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean fastHorizApproach = hvelAtOrAboveLimit && cstcHdistWithinHorizDist;
        boolean fastVertApproach = vvelAtOrAboveLimit && cstcVdistWithinVertDist;

        // --- Mode-nested transitions, highest priority first ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh19: OCM handles only reqMOM, reqVel and reqHdng.
            if (event instanceof InputEvent.ReqMOM && velWithinMomEntryLimit && !inOpez
                    && cdynBeyondMomEntryDist && cstcBeyondMomEntryDist) {
                // LRE-Beh4
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            } else if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel reqVelEvent = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(reqVelEvent.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng reqHdngEvent = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(reqHdngEvent.value()));
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
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                // LRE-Beh12
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (fastHorizApproach && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (fastVertApproach && !inOpez && !collisionCourse && !fastHorizApproach) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (cstcVdistWithinDfltVertDist && !inOpez && !collisionCourse && !fastHorizApproach
                    && !fastVertApproach) {
                // LRE-Beh10
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
            } else if (!cstcHdistWithinHorizDist && !cstcVdistWithinVertDist && !inOpez && !collisionCourse) {
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
