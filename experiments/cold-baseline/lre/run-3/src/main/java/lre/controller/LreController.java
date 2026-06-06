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
 * LRE controller — single-method, mode-nested if-else state machine.
 *
 * Each step:
 *   1. Invoke all operations to refresh derived quantities.
 *   2. Declare named boolean predicates for all guards.
 *   3. Outer if-else: exactly one block per mode (currentMode == X).
 *   4. Inner if-else: transitions ordered by priority.
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
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
    }

    public LreMode currentMode() { return currentMode; }

    public void step(InputEvent event) {
        // --- Refresh derived quantities (each step, before evaluating guards) ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velBelowOne = calcVel.vel() <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaBelowSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean cdaAboveSafe = calcCPA.cda() >= LreConstants.minSafeDist;
        boolean hvelAboveOne = calcVel.hvel() >= 1.0;
        boolean vvelAboveOne = calcVel.vvel() >= 1.0;
        boolean hdistCstcBelowHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcBelowDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean vdistCstcBelowVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;

        // ----- Outer if-else over mode -----
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: reqVel pass-through
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
            }
            // LRE-Beh3: reqHdng pass-through
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
            }
            // LRE-Beh4: OCM -> MOM (reqMOM + guards)
            else if (event instanceof InputEvent.ReqMOM
                    && velBelowOne
                    && !inOpez
                    && odistCdynAboveOne
                    && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }
        }
        else if (currentMode == LreMode.MOM) {
            // LRE-Beh6: MOM -> OCM (reqOCM) — bare-precondition for deadlock-freedom
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: MOM -> OCM (endTask, advVel(0))
            else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh12: MOM -> HCM (reqHCM)
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh8: MOM -> CAM (cda < minSafe && tcpa >= 0)
            else if (cdaBelowSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh5: MOM -> OCM (inOpez)
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh9: MOM -> HCM (hvel >= 1 && hdist(cstc) <= staticObsHorizDist)
            else if (hvelAboveOne && hdistCstcBelowHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: MOM -> HCM (vvel >= 1 && vdist(cstc) <= staticObsVertDist)
            else if (vvelAboveOne && vdistCstcBelowVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: MOM -> HCM (vdist(cstc) <= staticObsDfltVertDist)
            else if (vdistCstcBelowDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
        else if (currentMode == LreMode.HCM) {
            // LRE-Beh15: HCM -> OCM (reqOCM) — bare-precondition for deadlock-freedom
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: HCM -> CAM (cda < minSafe && tcpa >= 0)
            else if (cdaBelowSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh16: HCM -> OCM (inOpez)
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh13: HCM -> MOM (hdist > staticObsHorizDist && vdist > staticObsVertDist)
            else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }
        }
        else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: CAM -> OCM (reqOCM) — bare-precondition for deadlock-freedom
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: CAM -> OCM (cda >= minSafe), advVel(0)
            else if (cdaAboveSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
