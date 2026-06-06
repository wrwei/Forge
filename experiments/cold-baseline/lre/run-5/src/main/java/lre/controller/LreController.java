package lre.controller;

import lre.actuator.Actuator;
import lre.annotation.RoboChartType;
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
 * Initial mode is OCM.
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

    // Ctrl_State variables (LRE-Var1..8) mirrored on the controller after the operation step.
    private boolean inOpez;

    @RoboChartType("real")
    private double hvel;

    @RoboChartType("real")
    private double vvel;

    @RoboChartType("real")
    private double vel;

    @RoboChartType("nat")
    private int cstc;

    @RoboChartType("nat")
    private int cdyn;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
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

    public boolean inOpez() { return inOpez; }
    public double hvel() { return hvel; }
    public double vvel() { return vvel; }
    public double vel() { return vel; }
    public int cstc() { return cstc; }
    public int cdyn() { return cdyn; }
    public double cda() { return cda; }
    public double tcpa() { return tcpa; }

    public void step(InputEvent event) {
        // --- Refresh derived quantities each step (LRE-OP1..5) ---
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOPEZ.inOpez();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        // --- Named boolean predicates (one per atomic guard) ---
        boolean velLeq1 = vel <= 1.0;
        boolean odistCdynAbove1 = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAbove1 = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeast1 = hvel >= 1.0;
        boolean hdistCstcLeqHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcLeqDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAtLeast1 = vvel >= 1.0;
        boolean vdistCstcLeqVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(cstc) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(cstc) > LreConstants.staticObsVertDist;
        boolean cdaAtLeastMinSafe = cda >= LreConstants.minSafeDist;

        // --- Pure two-level mode-nested if-else ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: reqVel -> passthrough advVel, stay in OCM (bare-precondition)
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh3: reqHdng -> passthrough advHdng, stay in OCM
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh4: reqMOM with safety guards -> MOM
            else if (event instanceof InputEvent.ReqMOM && velLeq1 && !inOpez && odistCdynAbove1 && odistCstcAbove1) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh5: inOpez -> OCM (autonomous, highest priority safety override)
            if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh8: cda < minSafeDist AND tcpa >= 0 -> CAM
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh6: reqOCM -> OCM (bare-precondition event branch)
            else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7: endTask -> OCM, advise velocity 0
            else if (event instanceof InputEvent.EndTask) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh12: reqHCM -> HCM
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh9: hvel >= 1 AND hdist(cstc) <= staticObsHorizDist -> HCM
            else if (hvelAtLeast1 && hdistCstcLeqHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh10: vdist(cstc) <= staticObsDfltVertDist -> HCM
            else if (vdistCstcLeqDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh11: vvel >= 1 AND vdist(cstc) <= staticObsVertDist -> HCM
            else if (vvelAtLeast1 && vdistCstcLeqVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh16: inOpez -> OCM (highest-priority safety override)
            if (inOpez) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh14: cda < minSafeDist AND tcpa >= 0 -> CAM
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh15: reqOCM -> OCM (bare-precondition event branch)
            else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh13: hdist(cstc) > staticObsHorizDist AND vdist(cstc) > staticObsVertDist -> MOM
            else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: reqOCM -> OCM (bare-precondition event branch)
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18: cda >= minSafeDist -> OCM, advise velocity 0
            else if (cdaAtLeastMinSafe) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
        }
    }
}
