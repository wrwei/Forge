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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2): reads the AUV's sensor,
 * receives operator events and advises velocity and heading to the autopilot
 * through the actuator.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;

    private final CalcVel velCalc;
    private final CalcCStc cstcCalc;
    private final CalcCDyn cdynCalc;
    private final CheckOPEZ opezCheck;
    private final CalcCPA cpaCalc;

    /** Creates the controller in OCM over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velCalc = new CalcVel(sensor);
        this.cstcCalc = new CalcCStc(sensor);
        this.cdynCalc = new CalcCDyn(sensor);
        this.opezCheck = new CheckOPEZ(sensor);
        this.cpaCalc = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: runs the operations (LRE-OP1 to LRE-OP5), evaluates
     * the guard predicates, then takes at most one transition of the current
     * mode, highest priority first.
     *
     * <ul>
     *   <li>OCM: reqVel passed through (LRE-Beh2); reqHdng passed through
     *       (LRE-Beh3); reqMOM to MOM if allowed (LRE-Beh4); nothing else
     *       (LRE-Beh19).</li>
     *   <li>MOM: reqOCM (LRE-Beh6); endTask (LRE-Beh7); inOpez (LRE-Beh5);
     *       collision course to CAM (LRE-Beh8); horizontal approach
     *       (LRE-Beh9), vertical approach (LRE-Beh11), default vertical
     *       proximity (LRE-Beh10) and reqHCM (LRE-Beh12) to HCM.</li>
     *   <li>HCM: reqOCM (LRE-Beh15); inOpez (LRE-Beh16); collision course to
     *       CAM (LRE-Beh14); clear of the static obstacle to MOM (LRE-Beh13).</li>
     *   <li>CAM: reqOCM (LRE-Beh17); cda safe again to OCM (LRE-Beh18).</li>
     * </ul>
     * Entering MOM advises 1 m/s (LRE-FR2) and entering HCM advises 0 m/s
     * (LRE-FR3).
     *
     * @param event the operator event received this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        velCalc.compute();
        cstcCalc.compute();
        cdynCalc.compute();
        opezCheck.compute();
        cpaCalc.compute();

        boolean inOpez = opezCheck.inOpez();
        boolean velWithinEntryLimit = velCalc.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;
        boolean cdynBeyondEntryDist = sensor.odist(cdynCalc.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cstcBeyondEntryDist = sensor.odist(cstcCalc.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;
        boolean cdaBelowMinSafe = cpaCalc.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = cpaCalc.tcpa() >= LreConstants.MIN_TCPA;
        boolean hvelAtOrAboveLimit = velCalc.hvel() >= LreConstants.HORIZ_VEL_LIMIT;
        boolean vvelAtOrAboveLimit = velCalc.vvel() >= LreConstants.VERT_VEL_LIMIT;
        boolean cstcWithinHorizDist = sensor.hdist(cstcCalc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean cstcWithinVertDist = sensor.vdist(cstcCalc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean cstcWithinDfltVertDist = sensor.vdist(cstcCalc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean momEntryAllowed = velWithinEntryLimit && !inOpez && cdynBeyondEntryDist && cstcBeyondEntryDist;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean horizApproach = hvelAtOrAboveLimit && cstcWithinHorizDist;
        boolean vertApproach = vvelAtOrAboveLimit && cstcWithinVertDist;
        boolean cstcClear = !cstcWithinHorizDist && !cstcWithinVertDist;

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
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VELOCITY));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (collisionCourse && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (horizApproach && !inOpez && !collisionCourse) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (vertApproach && !inOpez && !collisionCourse && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (cstcWithinDfltVertDist && !inOpez && !collisionCourse && !horizApproach
                    && !vertApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VELOCITY));
            } else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionCourse && !horizApproach
                    && !vertApproach && !cstcWithinDfltVertDist) {
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
            } else if (cstcClear && !inOpez && !collisionCourse) {
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
