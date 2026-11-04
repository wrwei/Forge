package lre.controller;

import lre.LreConstants;
import lre.actuator.Actuator;
import lre.event.InputEvent;
import lre.event.OutputEvent;
import lre.mode.LreMode;
import lre.operation.CalcCDyn;
import lre.operation.CalcCPA;
import lre.operation.CalcCStc;
import lre.operation.CalcVel;
import lre.operation.CheckOPEZ;
import lre.sensor.Sensor;

/**
 * The Last Response Engine: mediates between the operator controller and the
 * autopilot of the AUV (LRE-ARCH1, LRE-ARCH2).
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

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velOp = new CalcVel(sensor);
        this.cstcOp = new CalcCStc(sensor);
        this.cdynOp = new CalcCDyn(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.cpaOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: recompute the derived quantities, then take at most one
     * transition. {@code event} is the operator input of this cycle, or
     * {@code null} when there is none.
     */
    public void step(InputEvent event) {
        // --- Operations: derived quantities, before the guards are evaluated ---
        velOp.compute();
        cstcOp.compute();
        cdynOp.compute();
        opezOp.compute();
        cpaOp.compute();

        // --- Named boolean predicates ---
        boolean inOpez = opezOp.inOpez();
        boolean velAtOrBelowMomLimit = velOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean dynObsBeyondMomDist = sensor.odist(cdynOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_OBS_DIST;
        boolean staticObsBeyondMomDist = sensor.odist(cstcOp.cstc()) > LreConstants.MOM_ENTRY_MIN_OBS_DIST;
        boolean cdaBelowMinSafe = cpaOp.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaOp.tcpa() >= 0.0;
        boolean collisionPredicted = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveHigh = velOp.hvel() >= LreConstants.HIGH_HORIZ_VEL;
        boolean vvelAtOrAboveHigh = velOp.vvel() >= LreConstants.HIGH_VERT_VEL;
        boolean staticHdistWithinThreshold = sensor.hdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVdistWithinThreshold = sensor.vdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVdistWithinDfltThreshold = sensor.vdist(cstcOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean fastNearStaticHoriz = hvelAtOrAboveHigh && staticHdistWithinThreshold;
        boolean fastNearStaticVert = vvelAtOrAboveHigh && staticVdistWithinThreshold;

        // --- Two-level if-else: one block per mode, transitions in priority order ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {                                   // LRE-Beh2
                InputEvent.ReqVel requestedVel = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(requestedVel.value()));
            } else if (event instanceof InputEvent.ReqHdng) {                           // LRE-Beh3
                InputEvent.ReqHdng requestedHdng = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(requestedHdng.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtOrBelowMomLimit && !inOpez
                    && dynObsBeyondMomDist && staticObsBeyondMomDist) {                 // LRE-Beh4
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADV_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            // Operator override first, then the safety exits, then the rest.
            if (event instanceof InputEvent.ReqOCM) {                                   // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (inOpez) {                                                        // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (collisionPredicted && !inOpez) {                                 // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.EndTask && !inOpez && !collisionPredicted) { // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADV_VEL));
            } else if (fastNearStaticHoriz && !inOpez && !collisionPredicted) {         // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADV_VEL));
            } else if (fastNearStaticVert && !inOpez && !collisionPredicted
                    && !fastNearStaticHoriz) {                                          // LRE-Beh11
                // Before Beh10: with equal vertical thresholds Beh10 would otherwise mask it.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADV_VEL));
            } else if (staticVdistWithinDfltThreshold && !inOpez && !collisionPredicted
                    && !fastNearStaticHoriz && !fastNearStaticVert) {                   // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADV_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionPredicted
                    && !fastNearStaticHoriz && !fastNearStaticVert
                    && !staticVdistWithinDfltThreshold) {                               // LRE-Beh12
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADV_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {                                   // LRE-Beh15
                currentMode = LreMode.OCM;
            } else if (inOpez) {                                                        // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (collisionPredicted && !inOpez) {                                 // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (!staticHdistWithinThreshold && !staticVdistWithinThreshold
                    && !inOpez && !collisionPredicted) {                                // LRE-Beh13
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADV_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {                                   // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {                                              // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_ADV_VEL));
            }
        }
    }
}
