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
 * LreController -- the Last Response Engine safety controller for the
 * AUV. Mode-nested if-else state machine with named boolean predicates.
 *
 * <p>The controller drives five operations each step (CalcVel,
 * CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA), declares named guard
 * predicates, then runs a pure two-level if-else: outer on currentMode,
 * inner on event type and predicates ordered by priority.
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

    // --- controller state variables (LRE-Var1..8) ---

    private boolean inOpez;

    @lre.annotation.RoboChartType("real")
    private double hvel;

    @lre.annotation.RoboChartType("real")
    private double vvel;

    @lre.annotation.RoboChartType("real")
    private double vel;

    @lre.annotation.RoboChartType("nat")
    private int cstc = -1;

    @lre.annotation.RoboChartType("nat")
    private int cdyn = -1;

    @lre.annotation.RoboChartType("real")
    private double cda;

    @lre.annotation.RoboChartType("real")
    private double tcpa;

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
        // --- run operations once per step to update derived quantities ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // --- snapshot operation outputs into controller state variables ---
        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOPEZ.inOpez();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        // --- named boolean predicates (declared BEFORE the if-else) ---
        boolean velLeOne = vel <= 1.0;
        boolean odistCdynGtOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcGtOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNeg = tcpa >= 0.0;
        boolean hvelGeOne = hvel >= 1.0;
        boolean hdistCstcLeHorizThresh = sensor.hdist(cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcLeDfltThresh = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean vvelGeOne = vvel >= 1.0;
        boolean vdistCstcLeVertThresh = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean hdistCstcGtHorizThresh = sensor.hdist(cstc) > LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcGtVertThresh = sensor.vdist(cstc) > LreConstants.STATIC_OBS_VERT_DIST;
        boolean cdaAboveOrEqMinSafe = cda >= LreConstants.MIN_SAFE_DIST;

        // --- mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: reqVel pass-through, remain in OCM
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel ev = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(ev.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh3: reqHdng pass-through, remain in OCM
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng ev = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(ev.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh4: reqMOM AND guards -> MOM
            else if (event instanceof InputEvent.ReqMOM
                    && velLeOne && !inOpez && odistCdynGtOne && odistCstcGtOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh6: reqOCM -> OCM (bare event, no extra guard)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: endTask -> OCM with advVel(0)
            else if (event instanceof InputEvent.EndTask) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh12: reqHCM -> HCM
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh5: inOpez -> OCM
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh8: cda < minSafeDist AND tcpa >= 0 -> CAM
            else if (cdaBelowMinSafe && tcpaNonNeg) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh9: hvel >= 1 AND hdist(cstc) <= staticObsHorizDist -> HCM
            else if (hvelGeOne && hdistCstcLeHorizThresh) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: vdist(cstc) <= staticObsDfltVertDist -> HCM
            else if (vdistCstcLeDfltThresh) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: vvel >= 1 AND vdist(cstc) <= staticObsVertDist -> HCM
            else if (vvelGeOne && vdistCstcLeVertThresh) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh15: reqOCM -> OCM (bare event, no extra guard)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh16: inOpez -> OCM
            else if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: cda < minSafeDist AND tcpa >= 0 -> CAM
            else if (cdaBelowMinSafe && tcpaNonNeg) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh13: hdist(cstc) > staticObsHorizDist AND vdist(cstc) > staticObsVertDist -> MOM
            else if (hdistCstcGtHorizThresh && vdistCstcGtVertThresh) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: reqOCM -> OCM (bare event, no extra guard)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: cda >= minSafeDist -> OCM with advVel(0)
            else if (cdaAboveOrEqMinSafe) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
        }
    }
}
