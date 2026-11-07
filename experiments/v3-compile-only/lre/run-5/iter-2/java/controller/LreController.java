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
 * The Last Response Engine: the AUV's safety controller (LRE-ARCH1, LRE-ARCH2).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velOp;
    private final CalcCStc cstcOp;
    private final CalcCDyn cdynOp;
    private final CheckOPEZ opezOp;
    private final CalcCPA cpaOp;

    /**
     * @param sensor   the AUV sensor service
     * @param actuator the receiver of the LRE's advice
     */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velOp = new CalcVel(sensor);
        this.cstcOp = new CalcCStc(sensor);
        this.cdynOp = new CalcCDyn(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.cpaOp = new CalcCPA(sensor);
    }

    /** @return the current operating mode */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle.
     *
     * @param event the operator input received this cycle, or null if none
     */
    public void step(InputEvent event) {
        // Derived quantities, computed before the guards are evaluated.
        velOp.compute();
        cstcOp.compute();
        cdynOp.compute();
        opezOp.compute();
        cpaOp.compute();

        // --- Named guard predicates ---
        boolean inOpez = opezOp.inOpez();
        boolean velAtMostOne = velOp.vel() <= 1.0;
        boolean dynObsBeyondOne = sensor.odist(cdynOp.cdyn()) > 1.0;
        boolean staticObsBeyondOne = sensor.odist(cstcOp.cstc()) > 1.0;
        boolean cdaAtLeastMinSafe = cpaOp.cda() >= LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaOp.tcpa() >= 0.0;
        boolean collisionCourse = !cdaAtLeastMinSafe && tcpaNonNegative;
        boolean hvelAtLeastOne = velOp.hvel() >= 1.0;
        boolean vvelAtLeastOne = velOp.vvel() >= 1.0;
        boolean staticHorizNear = sensor.hdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertNear = sensor.vdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticDfltVertNear = sensor.vdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizVelHazard = hvelAtLeastOne && staticHorizNear;
        boolean vertVelHazard = vvelAtLeastOne && staticVertNear;
        boolean staticObsClear = !staticHorizNear && !staticVertNear;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2: pass the requested velocity through.
                InputEvent.ReqVel velRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3: pass the requested heading through.
                InputEvent.ReqHdng hdngRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(hdngRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtMostOne && !inOpez && dynObsBeyondOne && staticObsBeyondOne) {
                // LRE-Beh4; MOM entry advises 1 m/s (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // Autonomous (safety) transitions first; operator events below
            // them carry the negation of every autonomous guard.
            if (inOpez) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (horizVelHazard && !inOpez && !collisionCourse) {
                // LRE-Beh9; HCM entry advises 0 m/s (LRE-FR3).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vertVelHazard && !inOpez && !collisionCourse && !horizVelHazard) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (staticDfltVertNear && !inOpez && !collisionCourse && !horizVelHazard && !vertVelHazard) {
                // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqOCM && !inOpez && !collisionCourse && !horizVelHazard && !vertVelHazard && !staticDfltVertNear) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionCourse && !horizVelHazard && !vertVelHazard && !staticDfltVertNear) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse && !horizVelHazard && !vertVelHazard && !staticDfltVertNear) {
                // LRE-Beh12
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (inOpez) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (staticObsClear && !inOpez && !collisionCourse) {
                // LRE-Beh13; MOM entry advises 1 m/s (LRE-FR2).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            } else if (event instanceof InputEvent.ReqOCM && !inOpez && !collisionCourse && !staticObsClear) {
                // LRE-Beh15
                currentMode = LreMode.OCM;
            }

        } else if (currentMode == LreMode.CAM) {
            if (cdaAtLeastMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqOCM && !cdaAtLeastMinSafe) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            }
        }
    }
}
