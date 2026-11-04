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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOpez;
    private final CalcCPA calcCpa;

    /** A controller reading {@code sensor} and advising through {@code actuator}. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOpez = new CheckOPEZ(sensor);
        this.calcCpa = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() { return currentMode; }

    /**
     * One control cycle. Runs the operations, then takes at most one
     * transition of the current mode. {@code event} is the operator input of
     * this cycle, or null if there is none.
     */
    public void step(InputEvent event) {
        // Operations, before any guard is evaluated; CheckOPEZ and CalcCPA after the index they use.
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOpez.compute();
        calcCpa.compute();

        // --- Named guard predicates ---
        boolean inOpez = checkOpez.inOpez();
        boolean velAtOrBelowEngageMax = calcVel.vel() <= LreConstants.ENGAGE_MAX_VEL;
        boolean dynObsBeyondEngageDist = sensor.odist(calcCDyn.cdyn()) > LreConstants.ENGAGE_MIN_DIST;
        boolean staticObsBeyondEngageDist = sensor.odist(calcCStc.cstc()) > LreConstants.ENGAGE_MIN_DIST;
        boolean cdaBelowMinSafe = calcCpa.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = calcCpa.tcpa() >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtOrAboveHigh = calcVel.hvel() >= LreConstants.HIGH_HORIZ_VEL;
        boolean vvelAtOrAboveHigh = calcVel.vvel() >= LreConstants.HIGH_VERT_VEL;
        boolean staticHdistWithinHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean staticVdistWithinVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean staticVdistWithinDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean fastNearStaticHoriz = hvelAtOrAboveHigh && staticHdistWithinHoriz;
        boolean fastNearStaticVert = vvelAtOrAboveHigh && staticVdistWithinVert;

        // --- One block per mode; inside, transitions in priority order ---
        if (currentMode == LreMode.OCM) {
            // OCM handles only reqVel, reqHdng and reqMOM (LRE-Beh19).
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel velocityRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velocityRequest.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng headingRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(headingRequest.value()));
            } else if (event instanceof InputEvent.ReqMOM && velAtOrBelowEngageMax && !inOpez
                    && dynObsBeyondEngageDist && staticObsBeyondEngageDist) {
                // LRE-Beh4; entering MOM advises 1 m/s (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6: operator override
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
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse) {
                // LRE-Beh12; entering HCM advises 0 m/s (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (fastNearStaticHoriz && !inOpez && !collisionCourse) {
                // LRE-Beh9
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (fastNearStaticVert && !inOpez && !collisionCourse && !fastNearStaticHoriz) {
                // LRE-Beh11
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            } else if (staticVdistWithinDfltVert && !inOpez && !collisionCourse && !fastNearStaticHoriz
                    && !fastNearStaticVert) {
                // LRE-Beh10
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15: operator override
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (!staticHdistWithinHoriz && !staticVdistWithinVert && !inOpez && !collisionCourse) {
                // LRE-Beh13; entering MOM advises 1 m/s (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_ADVISED_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17: operator override
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
