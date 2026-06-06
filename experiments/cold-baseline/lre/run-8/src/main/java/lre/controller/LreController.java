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

public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOPEZ;
    private final CalcCPA calcCPA;

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
        this.inOpez = false;
        this.hvel = 0.0;
        this.vvel = 0.0;
        this.vel = 0.0;
        this.cstc = -1;
        this.cdyn = -1;
        this.cda = Double.MAX_VALUE;
        this.tcpa = 0.0;
    }

    public LreMode currentMode() { return currentMode; }

    public void step(InputEvent event) {
        // Compute derived quantities each step
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

        // Named boolean predicates (declared BEFORE the if-else chain)
        boolean velAtMostOne = vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean vdistCstcAtMostVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(cstc) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(cstc) > LreConstants.staticObsVertDist;
        boolean cdaAtLeastMinSafe = cda >= LreConstants.minSafeDist;

        // Pure mode-nested if-else: every outer branch is currentMode == X
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
            } else if (event instanceof InputEvent.ReqMOM
                    && velAtMostOne && !inOpez && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (hvelAtLeastOne && hdistCstcAtMostHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vdistCstcAtMostDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vvelAtLeastOne && vdistCstcAtMostVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (cdaAtLeastMinSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
