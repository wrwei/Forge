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
 * Last Response Engine controller — a single-method, mode-nested if-else
 * state machine. Each step:
 *  1. Invokes the operations to refresh derived quantities.
 *  2. Declares named boolean predicates for all guard conditions.
 *  3. Evaluates transitions in priority order within the current mode.
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

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOpez = new CheckOPEZ(sensor, calcCStc);
        this.calcCpa = new CalcCPA(sensor, calcCDyn);
    }

    public LreMode currentMode() { return currentMode; }

    public void step(InputEvent event) {
        // --- Refresh derived quantities ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOpez.compute();
        calcCpa.compute();

        // --- Named boolean predicates (no inline complex expressions) ---
        boolean inOpez = checkOpez.inOpez();
        boolean velLe1 = calcVel.vel() <= 1.0;
        boolean odistCDynGt1 = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCStcGt1 = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCpa.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCpa.tcpa() >= 0.0;
        boolean hvelAtLeast1 = calcVel.hvel() >= 1.0;
        boolean hdistCStcLeHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean vdistCStcLeDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAtLeast1 = calcVel.vvel() >= 1.0;
        boolean vdistCStcLeVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean hdistCStcGtHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCStcGtVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;
        boolean cdaAtLeastMinSafe = calcCpa.cda() >= LreConstants.minSafeDist;

        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: reqVel → passthrough advVel
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
            }
            // LRE-Beh3: reqHdng → passthrough advHdng
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
            }
            // LRE-Beh4: reqMOM + guards → MOM, entry advVel(1)
            else if (event instanceof InputEvent.ReqMOM
                    && velLe1 && !inOpez && odistCDynGt1 && odistCStcGt1) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh5: inOpez → OCM (highest safety priority)
            if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh6: reqOCM → OCM
            else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: endTask → OCM, advVel(0)
            else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh8: cda < minSafeDist AND tcpa >= 0 → CAM
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh12: reqHCM → HCM
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh9: hvel >= 1 AND hdist(cstc) <= staticObsHorizDist → HCM
            else if (hvelAtLeast1 && hdistCStcLeHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: vdist(cstc) <= staticObsDfltVertDist → HCM
            else if (vdistCStcLeDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: vvel >= 1 AND vdist(cstc) <= staticObsVertDist → HCM
            else if (vvelAtLeast1 && vdistCStcLeVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh16: inOpez → OCM (highest safety priority)
            if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh15: reqOCM → OCM
            else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: cda < minSafeDist AND tcpa >= 0 → CAM
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13: hdist(cstc) > staticObsHorizDist AND vdist(cstc) > staticObsVertDist → MOM, entry advVel(1)
            else if (hdistCStcGtHoriz && vdistCStcGtVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: reqOCM → OCM
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: cda >= minSafeDist → OCM, advVel(0)
            else if (cdaAtLeastMinSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
