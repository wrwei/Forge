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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2): mediates between the
 * operator controller and the AUV's autopilot. Each control cycle it computes
 * the derived quantities from the sensor and then takes at most one mode
 * transition.
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

    /** A controller in OCM reading {@code sensor} and advising through {@code actuator}. */
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
     * One control cycle. {@code event} is the operator input received in this
     * cycle, or {@code null} if there was none.
     */
    public void step(InputEvent event) {
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOpezOp.compute();
        calcCpaOp.compute();

        boolean inOpez = checkOpezOp.inOpez();
        boolean velAtMostNormal = calcVelOp.vel() <= LreConstants.NORMAL_SPEED;
        boolean dynObsBeyondSafeDist = sensor.odist(calcCDynOp.cdyn()) > LreConstants.MIN_SAFE_DIST;
        boolean staticObsBeyondSafeDist = sensor.odist(calcCStcOp.cstc()) > LreConstants.MIN_SAFE_DIST;
        boolean momEntryAllowed = velAtMostNormal && !inOpez && dynObsBeyondSafeDist && staticObsBeyondSafeDist;
        boolean cdaBelowSafeDist = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionRisk = cdaBelowSafeDist && tcpaNonNegative;
        boolean hvelAtLeastNormal = calcVelOp.hvel() >= LreConstants.NORMAL_SPEED;
        boolean vvelAtLeastNormal = calcVelOp.vvel() >= LreConstants.NORMAL_SPEED;
        boolean staticHorizNear = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertNear = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticDfltVertNear = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtLeastNormal && staticHorizNear;
        boolean vertApproach = vvelAtLeastNormal && staticVertNear;
        boolean staticClear = !staticHorizNear && !staticVertNear;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel request = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(request.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng request = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(request.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.NORMAL_SPEED));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionRisk) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_SPEED));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionRisk) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_SPEED));
            } else if (horizApproach && !inOpez && !collisionRisk) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_SPEED));
            } else if (vertApproach && !inOpez && !collisionRisk && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_SPEED));
            } else if (staticDfltVertNear && !inOpez && !collisionRisk && !horizApproach && !vertApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_SPEED));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (staticClear && !inOpez && !collisionRisk) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.NORMAL_SPEED));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_SPEED));
            }
        }
    }
}
