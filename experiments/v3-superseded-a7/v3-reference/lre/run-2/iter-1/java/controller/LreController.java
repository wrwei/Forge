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
 * The Last Response Engine: the AUV's safety controller, between the operator
 * controller and the autopilot (LRE-ARCH1, LRE-ARCH2). Each cycle it runs the
 * five operations on the sensor data, then takes at most one transition of
 * its mode state machine.
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel velocityCalc;
    private final CalcCStc staticCalc;
    private final CheckOPEZ opezCheck;
    private final CalcCDyn dynamicCalc;
    private final CalcCPA approachCalc;

    /** Creates the controller in OCM over the given sensor and actuator. */
    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.velocityCalc = new CalcVel(sensor);
        this.staticCalc = new CalcCStc(sensor);
        this.opezCheck = new CheckOPEZ(sensor);
        this.dynamicCalc = new CalcCDyn(sensor);
        this.approachCalc = new CalcCPA(sensor);
    }

    /** The current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the operator input received in this cycle, or null for none
     */
    public void step(InputEvent event) {
        // The operations run before any guard is evaluated (LRE-OP1..OP5).
        velocityCalc.compute();
        staticCalc.compute();
        opezCheck.compute();
        dynamicCalc.compute();
        approachCalc.compute();

        // Guard predicates.
        boolean inOpez = opezCheck.inOpez();
        boolean velAtMostOne = velocityCalc.vel() <= 1.0;
        boolean dynObsBeyondOne = sensor.odist(dynamicCalc.cdyn()) > 1.0;
        boolean stcObsBeyondOne = sensor.odist(staticCalc.cstc()) > 1.0;
        boolean momEntryAllowed = velAtMostOne && !inOpez && dynObsBeyondOne && stcObsBeyondOne;
        boolean cdaBelowMinSafe = approachCalc.cda() < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = approachCalc.tcpa() >= 0.0;
        boolean collisionRisk = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLeastOne = velocityCalc.hvel() >= 1.0;
        boolean vvelAtLeastOne = velocityCalc.vvel() >= 1.0;
        boolean stcHdistWithinHoriz = sensor.hdist(staticCalc.cstc()) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean stcVdistWithinVert = sensor.vdist(staticCalc.cstc()) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean stcVdistWithinDfltVert = sensor.vdist(staticCalc.cstc()) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean horizApproach = hvelAtLeastOne && stcHdistWithinHoriz;
        boolean vertApproach = vvelAtLeastOne && stcVdistWithinVert;
        boolean staticClear = !stcHdistWithinHoriz && !stcVdistWithinVert;

        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: pass the requested velocity through.
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel velocityRequest = (InputEvent.ReqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(velocityRequest.value()));
            }
            // LRE-Beh3: pass the requested heading through.
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng headingRequest = (InputEvent.ReqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvHdng(headingRequest.value()));
            }
            // LRE-Beh4: to MOM; entering MOM advises 1 m/s (LRE-FR2).
            else if (event instanceof InputEvent.ReqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
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
            else if (collisionRisk && !inOpez) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh7
            else if (event instanceof InputEvent.EndTask && !inOpez && !collisionRisk) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh12; entering HCM advises 0 m/s (LRE-FR3), as in the three below.
            else if (event instanceof InputEvent.ReqHCM && !inOpez && !collisionRisk) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh9
            else if (horizApproach && !inOpez && !collisionRisk) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11
            else if (vertApproach && !inOpez && !collisionRisk && !horizApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10
            else if (stcVdistWithinDfltVert && !inOpez && !collisionRisk && !horizApproach && !vertApproach) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
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
            else if (collisionRisk && !inOpez) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13; entering MOM advises 1 m/s (LRE-FR2).
            else if (staticClear && !inOpez && !collisionRisk) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18
            else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
