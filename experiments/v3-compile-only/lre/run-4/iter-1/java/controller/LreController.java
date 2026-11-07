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
 * operator controller and the AUV's autopilot. Each control cycle it runs the
 * operations over the sensor data and then takes at most one mode
 * transition.
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

    /** Creates the LRE in OCM over the given sensor and actuator. */
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
     * One control cycle.
     *
     * @param event the operator input received in this cycle, or {@code null}
     *              if there is none
     */
    public void step(InputEvent event) {
        calcVelOp.compute();
        calcCStcOp.compute();
        checkOpezOp.compute();
        calcCDynOp.compute();
        calcCpaOp.compute();

        boolean inOpez = checkOpezOp.inOpez();
        boolean velWithinLimit = calcVelOp.vel() <= LreConstants.VEL_LIMIT;
        boolean hvelAtOrAboveLimit = calcVelOp.hvel() >= LreConstants.VEL_LIMIT;
        boolean vvelAtOrAboveLimit = calcVelOp.vvel() >= LreConstants.VEL_LIMIT;
        boolean cdynBeyondEntryDist = sensor.odist(calcCDynOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcBeyondEntryDist = sensor.odist(calcCStcOp.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcHorizWithinLimit = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVertWithinLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVertWithinDfltLimit = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean cdaBelowMinSafe = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean horizApproach = hvelAtOrAboveLimit && cstcHorizWithinLimit;
        boolean vertApproach = vvelAtOrAboveLimit && cstcVertWithinLimit;
        boolean momEntryAllowed = velWithinLimit && !inOpez && cdynBeyondEntryDist && cstcBeyondEntryDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VELOCITY));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask && !inOpez) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (horizApproach && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (cstcVertWithinDfltLimit && !inOpez && !collisionCourse && !horizApproach
                    && !vertApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse && !horizApproach
                    && !vertApproach && !cstcVertWithinDfltLimit) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (!cstcHorizWithinLimit && !cstcVertWithinLimit && !inOpez && !collisionCourse) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VELOCITY));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            }
        }
    }
}
