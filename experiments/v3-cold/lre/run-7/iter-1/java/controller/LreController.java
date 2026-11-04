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
 * The Last Response Engine: the AUV's reactive safety controller between the operator
 * controller and the autopilot. It reads the sensor, computes derived quantities with its
 * operations each step, and advises velocity and heading through the actuator.
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
     * One control cycle.
     *
     * @param event the operator input received this cycle, or {@code null} if there is none
     */
    public void step(InputEvent event) {
        // Derived quantities for this cycle, computed before any guard is evaluated.
        calcVelOp.compute();
        calcCStcOp.compute();
        calcCDynOp.compute();
        checkOpezOp.compute();
        calcCpaOp.compute();

        boolean inOpez = checkOpezOp.inOpez();
        boolean velAtMostOne = calcVelOp.vel() <= 1.0;
        boolean cdynDistAboveOne = sensor.odist(calcCDynOp.cdyn()) > 1.0;
        boolean cstcDistAboveOne = sensor.odist(calcCStcOp.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpaOp.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLeastOne = calcVelOp.hvel() >= 1.0;
        boolean vvelAtLeastOne = calcVelOp.vvel() >= 1.0;
        boolean cstcHdistWithinHoriz = sensor.hdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcVdistWithinVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcVdistWithinDfltVert = sensor.vdist(calcCStcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizCaution = hvelAtLeastOne && cstcHdistWithinHoriz;
        boolean vertCaution = vvelAtLeastOne && cstcVdistWithinVert;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel requestedVel = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(requestedVel.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng requestedHdng = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(requestedHdng.value()));
            } else if (event instanceof InputEvent.ReqMOM
                    && velAtMostOne && !inOpez && cdynDistAboveOne && cstcDistAboveOne) {
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
            } else if (horizCaution && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertCaution && !inOpez && !collisionCourse && !horizCaution) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (cstcVdistWithinDfltVert && !inOpez && !collisionCourse && !horizCaution && !vertCaution) {
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
            } else if (!cstcHdistWithinHoriz && !cstcVdistWithinVert && !inOpez && !collisionCourse) {
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
