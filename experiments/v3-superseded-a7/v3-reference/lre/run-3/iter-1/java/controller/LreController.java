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
 * The Last Response Engine: mediates between the operator controller and the
 * AUV autopilot (LRE-ARCH1). Each step it runs its operations on the sensor
 * data, then evaluates the transitions of the current mode (LRE-ARCH2).
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

    /** Creates the controller in OCM over the given sensor and actuator. */
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
     * One control cycle. {@code event} is the operator input received in this
     * cycle, or {@code null} if there is none. In each mode the transitions are
     * tried in priority order: the operator's reqOCM, then OPEZ, then collision
     * risk, then the remaining operator requests, then the static-obstacle rules.
     */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        boolean inOpez = checkOPEZ.inOpez();
        boolean cdaBelowMinSafeDist = calcCPA.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean collisionPredicted = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean velAtOrBelowLimit = calcVel.vel() <= LreConstants.VEL_LIMIT;
        boolean hvelAtOrAboveLimit = calcVel.hvel() >= LreConstants.VEL_LIMIT;
        boolean vvelAtOrAboveLimit = calcVel.vvel() >= LreConstants.VEL_LIMIT;
        boolean dynamicObsBeyondClearance = sensor.odist(calcCDyn.cdyn()) > LreConstants.MOM_ENTRY_CLEARANCE;
        boolean staticObsBeyondClearance = sensor.odist(calcCStc.cstc()) > LreConstants.MOM_ENTRY_CLEARANCE;
        boolean momEntryAllowed = velAtOrBelowLimit && !inOpez && dynamicObsBeyondClearance && staticObsBeyondClearance;
        boolean staticHorizDistWithinLimit = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVertDistWithinLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVertDistWithinDfltLimit = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizontalApproach = hvelAtOrAboveLimit && staticHorizDistWithinLimit;
        boolean verticalApproach = vvelAtOrAboveLimit && staticVertDistWithinLimit;
        boolean staticObsClear = !staticHorizDistWithinLimit && !staticVertDistWithinLimit;

        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: pass the operator's velocity through.
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel requestedVel = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(requestedVel.value()));
            }
            // LRE-Beh3: pass the operator's heading through.
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng requestedHdng = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(requestedHdng.value()));
            }
            // LRE-Beh4: enter MOM; MOM entry advises 1 m/s (LRE-FR2).
            else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh6
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh5
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh8
            else if (collisionPredicted && !inOpez) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh7
            else if (event instanceof InputEvent.EndTask && !inOpez && !collisionPredicted) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
            // LRE-Beh12; HCM entry advises 0 m/s (LRE-FR3).
            else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionPredicted) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }
            // LRE-Beh9
            else if (horizontalApproach && !inOpez && !collisionPredicted) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }
            // LRE-Beh11
            else if (verticalApproach && !inOpez && !collisionPredicted && !horizontalApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }
            // LRE-Beh10
            else if (staticVertDistWithinDfltLimit && !inOpez && !collisionPredicted
                    && !horizontalApproach && !verticalApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh15
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh16
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14
            else if (collisionPredicted && !inOpez) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13; MOM entry advises 1 m/s (LRE-FR2).
            else if (staticObsClear && !inOpez && !collisionPredicted) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18
            else if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
