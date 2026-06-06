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
 * LRE safety controller (LRE-ARCH1, LRE-ARCH2).
 * <p>
 * Single-method, mode-nested if-else state machine. The outer if-else
 * chain selects the current mode; the inner if-else chains within each
 * block select transitions in priority order.
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

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        // --- Pre-evaluate operations (LRE-ARCH2) ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velAtMostOne = calcVel.vel() <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean hvelAtLeastOne = calcVel.hvel() >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAtLeastOne = calcVel.vvel() >= 1.0;
        boolean vdistCstcAtMostVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;
        boolean cdaAtLeastMinSafe = calcCPA.cda() >= LreConstants.minSafeDist;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: reqVel passthrough (stays in OCM).
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh3: reqHdng passthrough (stays in OCM).
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh4: OCM -> MOM on reqMOM if all guards hold.
            else if (event instanceof InputEvent.ReqMOM
                    && velAtMostOne
                    && !inOpez
                    && odistCdynAboveOne
                    && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh6: MOM -> OCM on reqOCM (bare event-triggered — highest priority).
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: MOM -> OCM on endTask, advise velocity 0.
            else if (event instanceof InputEvent.EndTask) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh12: MOM -> HCM on reqHCM.
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh5: MOM -> OCM when inOpez (autonomous).
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh8: MOM -> CAM on cda < minSafeDist && tcpa >= 0 (autonomous).
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh9: MOM -> HCM on hvel >= 1 && hdist(cstc) <= staticObsHorizDist.
            else if (hvelAtLeastOne && hdistCstcAtMostHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: MOM -> HCM on vdist(cstc) <= staticObsDfltVertDist.
            else if (vdistCstcAtMostDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: MOM -> HCM on vvel >= 1 && vdist(cstc) <= staticObsVertDist.
            else if (vvelAtLeastOne && vdistCstcAtMostVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh15: HCM -> OCM on reqOCM (bare event-triggered).
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh16: HCM -> OCM when inOpez (autonomous).
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: HCM -> CAM on cda < minSafeDist && tcpa >= 0.
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13: HCM -> MOM on hdist(cstc) > horiz && vdist(cstc) > vert.
            else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: CAM -> OCM on reqOCM (bare event-triggered).
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: CAM -> OCM on cda >= minSafeDist, advise velocity 0.
            else if (cdaAtLeastMinSafe) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
        }
    }
}
