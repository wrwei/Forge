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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2): mediates between the
 * operator controller and the AUV's autopilot. Each control cycle it computes
 * the derived quantities from the sensor, then evaluates the transitions of
 * the current mode.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOPEZ;
    private final CalcCPA calcCPA;

    /** Creates the controller, in OCM, over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor);
        this.calcCPA = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle.
     *
     * @param event the operator input received this cycle, or {@code null}
     *              if there is none
     */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // --- Named guard predicates ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtMostOne = calcVel.vel() <= 1.0;
        boolean dynObsBeyondOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean staticObsBeyondOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaAtLeastMinSafe = calcCPA.cda() >= LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean collisionRisk = !cdaAtLeastMinSafe && tcpaNonNegative;
        boolean hvelAtLeastOne = calcVel.hvel() >= 1.0;
        boolean vvelAtLeastOne = calcVel.vvel() >= 1.0;
        boolean staticHorizNear = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertNear = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticDfltVertNear = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizVelHazard = hvelAtLeastOne && staticHorizNear;
        boolean vertVelHazard = vvelAtLeastOne && staticVertNear;
        boolean staticObsClear = !staticHorizNear && !staticVertNear;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel request = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(request.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng request = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(request.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtMostOne && !inOpez
                    && dynObsBeyondOne && staticObsBeyondOne) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // Autonomous (guard-only) transitions first, then event-triggered ones.
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (horizVelHazard && !inOpez && !collisionRisk) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertVelHazard && !inOpez && !collisionRisk && !horizVelHazard) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (staticDfltVertNear && !inOpez && !collisionRisk && !horizVelHazard && !vertVelHazard) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqOCM && !inOpez && !collisionRisk && !horizVelHazard
                    && !vertVelHazard && !staticDfltVertNear) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionRisk && !horizVelHazard
                    && !vertVelHazard && !staticDfltVertNear) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionRisk && !horizVelHazard
                    && !vertVelHazard && !staticDfltVertNear) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionRisk && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (staticObsClear && !inOpez && !collisionRisk) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            } else if (event instanceof InputEvent.ReqOCM && !inOpez && !collisionRisk && !staticObsClear) {
                currentMode = LreMode.OCM;
            }

        } else if (currentMode == LreMode.CAM) {
            if (cdaAtLeastMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqOCM && !cdaAtLeastMinSafe) {
                currentMode = LreMode.OCM;
            }
        }
    }
}
