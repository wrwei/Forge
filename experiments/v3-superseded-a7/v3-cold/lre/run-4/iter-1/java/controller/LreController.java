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
 * The Last Response Engine, the AUV's safety controller (LRE-ARCH1,
 * LRE-ARCH2). It reads the sensor, runs its operations, and advises velocity
 * and heading to the autopilot controller through the actuator, switching
 * between four modes; it starts in OCM (LRE-Beh1).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velocityOp;
    private final CalcCStc closestStaticOp;
    private final CalcCDyn closestDynamicOp;
    private final CheckOPEZ opezOp;
    private final CalcCPA cpaOp;

    /** A controller reading the given sensor and advising through the given actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velocityOp = new CalcVel(sensor);
        this.closestStaticOp = new CalcCStc(sensor);
        this.closestDynamicOp = new CalcCDyn(sensor);
        this.opezOp = new CheckOPEZ(sensor);
        this.cpaOp = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: runs the operations, then takes at most one transition
     * of the current mode.
     *
     * @param event the operator input received in this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        // LRE-OP1..OP5: the operations run before any guard is evaluated.
        velocityOp.compute();
        closestStaticOp.compute();
        closestDynamicOp.compute();
        opezOp.compute();
        cpaOp.compute();

        // Guard predicates.
        boolean inOpez = opezOp.inOpez();                                                                       // LRE-GP1
        boolean velAtMostMomEntry = velocityOp.vel() <= LreConstants.MOM_ENTRY_MAX_VEL;                            // LRE-Beh4
        boolean dynamicBeyondMomEntry = sensor.odist(closestDynamicOp.cdyn()) > LreConstants.MOM_ENTRY_MIN_DIST;   // LRE-Beh4
        boolean staticBeyondMomEntry = sensor.odist(closestStaticOp.cstc()) > LreConstants.MOM_ENTRY_MIN_DIST;     // LRE-Beh4
        boolean momEntryAllowed = velAtMostMomEntry && !inOpez && dynamicBeyondMomEntry && staticBeyondMomEntry;  // LRE-Beh4
        boolean cdaBelowMinSafe = cpaOp.cda() < LreConstants.MIN_SAFE_DIST;                                       // LRE-Beh8, 14, 18
        boolean tcpaNonNegative = cpaOp.tcpa() >= 0.0;                                                           // LRE-Beh8, 14
        boolean collisionRisk = cdaBelowMinSafe && tcpaNonNegative;                                              // LRE-Beh8, 14
        boolean hvelAtOrAboveCaution = velocityOp.hvel() >= LreConstants.CAUTION_VEL;                              // LRE-Beh9
        boolean hdistStaticWithinHoriz = sensor.hdist(closestStaticOp.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;          // LRE-Beh9, 13
        boolean vvelAtOrAboveCaution = velocityOp.vvel() >= LreConstants.CAUTION_VEL;                              // LRE-Beh11
        boolean vdistStaticWithinVert = sensor.vdist(closestStaticOp.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;            // LRE-Beh11, 13
        boolean vdistStaticWithinDfltVert = sensor.vdist(closestStaticOp.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;   // LRE-Beh10
        boolean horizApproach = hvelAtOrAboveCaution && hdistStaticWithinHoriz;                                  // LRE-Beh9
        boolean vertApproach = vvelAtOrAboveCaution && vdistStaticWithinVert;                                    // LRE-Beh11

        if (currentMode == LreMode.OCM) {
            // LRE-Beh19: OCM handles exactly reqVel, reqHdng and reqMOM.
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2
                InputEvent.ReqVel request = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(request.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3
                InputEvent.ReqHdng request = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(request.value()));
            } else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                // LRE-Beh4; entry to MOM (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VEL));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6
                currentMode = LreMode.OCM;
            } else if (collisionRisk) {
                // LRE-Beh8
                currentMode = LreMode.CAM;
            } else if (inOpez && !collisionRisk) {
                // LRE-Beh5
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask && !collisionRisk && !inOpez) {
                // LRE-Beh7
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            } else if (event instanceof InputEvent.ReqHCM && !collisionRisk && !inOpez) {
                // LRE-Beh12; entry to HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (horizApproach && !collisionRisk && !inOpez) {
                // LRE-Beh9; entry to HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (vertApproach && !collisionRisk && !inOpez && !horizApproach) {
                // LRE-Beh11; entry to HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            } else if (vdistStaticWithinDfltVert && !collisionRisk && !inOpez && !horizApproach && !vertApproach) {
                // LRE-Beh10; entry to HCM (LRE-FR3)
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.HCM_VEL));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15
                currentMode = LreMode.OCM;
            } else if (collisionRisk) {
                // LRE-Beh14
                currentMode = LreMode.CAM;
            } else if (inOpez && !collisionRisk) {
                // LRE-Beh16
                currentMode = LreMode.OCM;
            } else if (!hdistStaticWithinHoriz && !vdistStaticWithinVert && !collisionRisk && !inOpez) {
                // LRE-Beh13; entry to MOM (LRE-FR2)
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.MOM_VEL));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(LreConstants.STOP_VEL));
            }
        }
    }
}
