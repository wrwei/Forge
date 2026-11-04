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
 * The Last Response Engine: mediates between the operator controller and the AUV's autopilot.
 * Each cycle it recomputes the derived quantities from the sensor, then decides whether to
 * change mode; it advises the autopilot only through advVel and advHdng.
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
     * One control cycle. {@code event} is the operator input received this cycle, or
     * {@code null} when there is none.
     */
    public void step(InputEvent event) {
        // Derived quantities, before any guard is evaluated.
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOpezOp.compute();
        calcCpaOp.compute();

        // Named guard predicates.
        boolean inOpez = checkOpezOp.inOpez();
        boolean velWithinLimit = calcVelOp.vel() <= LreConstants.VEL_LIMIT;
        boolean cdynBeyondSafeDist = sensor.odist(calcCDynOp.cdyn()) > LreConstants.MIN_SAFE_DIST;
        boolean cstcBeyondSafeDist = sensor.odist(calcCStcOp.cstc()) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowSafeDist = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowSafeDist && tcpaNonNegative;
        boolean hvelAtOrAboveLimit = calcVelOp.hvel() >= LreConstants.VEL_LIMIT;
        boolean cstcHdistWithinHoriz = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean horizApproachStatic = hvelAtOrAboveLimit && cstcHdistWithinHoriz;
        boolean vvelAtOrAboveLimit = calcVelOp.vvel() >= LreConstants.VEL_LIMIT;
        boolean cstcVdistWithinVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vertApproachStatic = vvelAtOrAboveLimit && cstcVdistWithinVert;
        boolean cstcVdistWithinDfltVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean staticObsClear = !cstcHdistWithinHoriz && !cstcVdistWithinVert;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && velWithinLimit && !inOpez
                    && cdynBeyondSafeDist && cstcBeyondSafeDist) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VELOCITY));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (horizApproachStatic && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (vertApproachStatic && !inOpez && !collisionCourse && !horizApproachStatic) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (cstcVdistWithinDfltVert && !inOpez && !collisionCourse && !horizApproachStatic
                    && !vertApproachStatic) {
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
            } else if (staticObsClear && !inOpez && !collisionCourse) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VELOCITY));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            }
        }
    }
}
