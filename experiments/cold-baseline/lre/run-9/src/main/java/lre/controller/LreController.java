package lre.controller;

import lre.actuator.Actuator;
import lre.constants.LreConstants;
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
 * Last Response Engine controller.
 *
 * Single-method, mode-nested if-else state machine. The controller
 * invokes its operations to compute derived quantities, then evaluates
 * named boolean predicates, then dispatches on the current mode.
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

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor, calcCStc);
        this.calcCPA = new CalcCPA(sensor, calcCDyn);
    }

    public LreMode currentMode() { return currentMode; }

    public void step(InputEvent event) {
        // --- Invoke operations to refresh derived quantities ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtOrBelowOne = calcVel.vel() <= 1.0;
        boolean odistCDynAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCStcAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean hvelAtOrAboveOne = calcVel.hvel() >= 1.0;
        boolean hdistCStcAtOrBelowHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean vdistCStcAtOrBelowDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAtOrAboveOne = calcVel.vvel() >= 1.0;
        boolean vdistCStcAtOrBelowVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean hdistCStcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCStcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;
        boolean cdaAtOrAboveMinSafe = calcCPA.cda() >= LreConstants.minSafeDist;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: OCM pass velocity
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
            }
            // LRE-Beh3: OCM pass heading
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
            }
            // LRE-Beh4: OCM -> MOM
            else if (event instanceof InputEvent.ReqMOM
                    && velAtOrBelowOne
                    && !inOpez
                    && odistCDynAboveOne
                    && odistCStcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh6: MOM -> OCM on reqOCM
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: MOM -> OCM on endTask
            else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh12: MOM -> HCM on reqHCM
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh5: MOM -> OCM when inOpez
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh8: MOM -> CAM on cda/tcpa
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh9: MOM -> HCM on horizontal velocity + horizontal distance
            else if (hvelAtOrAboveOne && hdistCStcAtOrBelowHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: MOM -> HCM on default vertical distance
            else if (vdistCStcAtOrBelowDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: MOM -> HCM on vertical velocity + vertical distance
            else if (vvelAtOrAboveOne && vdistCStcAtOrBelowVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh15: HCM -> OCM on reqOCM
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh16: HCM -> OCM when inOpez
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: HCM -> CAM on cda/tcpa
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13: HCM -> MOM when clear of static obstacle
            else if (hdistCStcAboveHoriz && vdistCStcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: CAM -> OCM on reqOCM
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: CAM -> OCM when safe
            else if (cdaAtOrAboveMinSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
