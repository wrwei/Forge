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
 * The Last Response Engine: the AUV's safety controller between the operator
 * controller and the autopilot (LRE-ARCH1, LRE-ARCH2).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVelOp;
    private final CalcCStc calcCStcOp;
    private final CheckOPEZ checkOPEZOp;
    private final CalcCDyn calcCDynOp;
    private final CalcCPA calcCPAOp;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVelOp = new CalcVel(sensor);
        this.calcCStcOp = new CalcCStc(sensor);
        this.checkOPEZOp = new CheckOPEZ(sensor);
        this.calcCDynOp = new CalcCDyn(sensor);
        this.calcCPAOp = new CalcCPA(sensor);
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
        // Operations run before any guard is evaluated (LRE-OP1..OP5).
        calcVelOp.compute();
        calcCStcOp.compute();
        checkOPEZOp.compute();
        calcCDynOp.compute();
        calcCPAOp.compute();

        // --- Named guard predicates ---
        boolean inOpez = checkOPEZOp.inOpez();
        boolean velAtOrBelowNormal = calcVelOp.vel() <= LreConstants.NORMAL_SPEED;
        boolean dynamicBeyondMinSafe = sensor.odist(calcCDynOp.cdyn()) > LreConstants.MIN_SAFE_DIST;
        boolean staticBeyondMinSafe = sensor.odist(calcCStcOp.cstc()) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowMinSafe = calcCPAOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPAOp.tcpa() >= 0.0;
        boolean onCollisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveNormal = calcVelOp.hvel() >= LreConstants.NORMAL_SPEED;
        boolean vvelAtOrAboveNormal = calcVelOp.vvel() >= LreConstants.NORMAL_SPEED;
        boolean staticHorizWithinLimit = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertWithinLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVertWithinDfltLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean fastNearStaticHoriz = hvelAtOrAboveNormal && staticHorizWithinLimit;
        boolean fastNearStaticVert = vvelAtOrAboveNormal && staticVertWithinLimit;

        // --- Mode-nested transitions, highest priority first ---
        if (currentMode == LreMode.OCM) {
            // OCM handles only reqVel, reqHdng and reqMOM (LRE-Beh19).
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel requestedVel = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(requestedVel.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng requestedHdng = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(requestedHdng.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtOrBelowNormal && !inOpez
                    && dynamicBeyondMinSafe && staticBeyondMinSafe) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.NORMAL_SPEED));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (onCollisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !onCollisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.CAUTION_SPEED));
            } else if (fastNearStaticHoriz && !inOpez && !onCollisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.CAUTION_SPEED));
            } else if (fastNearStaticVert && !inOpez && !onCollisionCourse && !fastNearStaticHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.CAUTION_SPEED));
            } else if (staticVertWithinDfltLimit && !inOpez && !onCollisionCourse && !fastNearStaticHoriz
                    && !fastNearStaticVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.CAUTION_SPEED));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (onCollisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (!staticHorizWithinLimit && !staticVertWithinLimit && !inOpez && !onCollisionCourse) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.NORMAL_SPEED));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HALT_SPEED));
            }
        }
    }
}
