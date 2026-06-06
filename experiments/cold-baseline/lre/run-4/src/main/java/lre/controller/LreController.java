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
 * LRE safety controller. Single-method, mode-nested if-else state machine.
 *
 * Initial mode: OCM (LRE-Beh1).
 *
 * At each step, the controller first runs operations (CalcVel, CalcCStc,
 * CalcCDyn, CalcCPA, CheckOPEZ), declares all named guard predicates, then
 * enters a pure mode-nested if-else chain.
 */
public final class LreController {

    private LreMode currentMode;
    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CalcCPA calcCPA;
    private final CheckOPEZ checkOPEZ;

    public LreController(Sensor sensor, Actuator actuator) {
        this.currentMode = LreMode.OCM;
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.calcCPA = new CalcCPA(sensor, calcCDyn);
        this.checkOPEZ = new CheckOPEZ(sensor, calcCStc);
    }

    public LreMode currentMode() { return currentMode; }

    public void step(InputEvent event) {
        // --- Run operations every step ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        calcCPA.compute();
        checkOPEZ.compute();

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velBelowOne = calcVel.vel() <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean cdaAboveMinSafe = calcCPA.cda() >= LreConstants.minSafeDist;
        boolean hvelAboveOne = calcVel.hvel() >= 1.0;
        boolean hdistCstcBelowHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcBelowDflt = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAboveOne = calcVel.vvel() >= 1.0;
        boolean vdistCstcBelowVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;

        // --- Pure mode-nested if-else ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: reqVel pass-through
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh3: reqHdng pass-through
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh4: OCM -> MOM on reqMOM with safety guards
            else if (event instanceof InputEvent.ReqMOM
                    && velBelowOne
                    && !inOpez
                    && odistCdynAboveOne
                    && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh6: MOM -> OCM on reqOCM (operator override, highest priority)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: MOM -> OCM on endTask, advise velocity 0
            else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh12: MOM -> HCM on reqHCM
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh5: MOM -> OCM when inOpez (autonomous)
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh8: MOM -> CAM when cda below threshold AND tcpa >= 0
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh9: MOM -> HCM when hvel >= 1 AND hdist(cstc) <= staticObsHorizDist
            else if (hvelAboveOne && hdistCstcBelowHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: MOM -> HCM when vdist(cstc) <= staticObsDfltVertDist
            else if (vdistCstcBelowDflt) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: MOM -> HCM when vvel >= 1 AND vdist(cstc) <= staticObsVertDist
            else if (vvelAboveOne && vdistCstcBelowVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh15: HCM -> OCM on reqOCM (highest priority operator override)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh16: HCM -> OCM when inOpez
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: HCM -> CAM when cda below threshold AND tcpa >= 0
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13: HCM -> MOM when hdist > horiz AND vdist > vert
            else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: CAM -> OCM on reqOCM (operator override, highest priority)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: CAM -> OCM when cda >= minSafeDist, advise velocity 0
            else if (cdaAboveMinSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
