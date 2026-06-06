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
 * The Last Response Engine safety controller (LRE-ARCH1, LRE-ARCH2).
 * A single-method, mode-nested if-else state machine over the four
 * operating modes OCM, MOM, HCM, CAM, starting in OCM (LRE-Beh1).
 * Each step the controller invokes the operations to refresh derived
 * quantities, then evaluates transition guards.
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

    private boolean inOpez;

    @RoboChartType("real")
    private double hvel;

    @RoboChartType("real")
    private double vvel;

    @RoboChartType("real")
    private double vel;

    private int cstc = -1;

    private int cdyn = -1;

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
        this.checkOpez = new CheckOPEZ(sensor);
        this.calcCpa = new CalcCPA(sensor);
    }

    /** Current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: refresh the derived quantities via the
     * operations, then evaluate event-triggered and autonomous
     * transitions for the current mode.
     */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOpez.compute();
        calcCpa.compute();
        hvel = calcVel.hvel();
        vvel = calcVel.vvel();
        vel = calcVel.vel();
        cstc = calcCStc.cstc();
        cdyn = calcCDyn.cdyn();
        inOpez = checkOpez.inOpez();
        cda = calcCpa.cda();
        tcpa = calcCpa.tcpa();

        boolean velAtMostOne = vel <= 1.0;
        boolean odistCdynAboveMinSafe = sensor.odist(cdyn) > LreConstants.minSafeDist;
        boolean odistCstcAboveMinSafe = sensor.odist(cstc) > LreConstants.minSafeDist;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCstcWithinHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcWithinDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean vdistCstcWithinVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean hdistCstcBeyondHoriz = sensor.hdist(cstc) > LreConstants.staticObsHorizDist;
        boolean vdistCstcBeyondVert = sensor.vdist(cstc) > LreConstants.staticObsVertDist;
        boolean cdaAtLeastMinSafe = cda >= LreConstants.minSafeDist;
        boolean camGuard = cdaBelowMinSafe && tcpaNonNegative;
        boolean hcmHorizGuard = hvelAtLeastOne && hdistCstcWithinHoriz;
        boolean hcmVertGuard = vvelAtLeastOne && vdistCstcWithinVert;
        boolean backToMomGuard = hdistCstcBeyondHoriz && vdistCstcBeyondVert;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                actuator.apply(new OutputEvent.advVel(rv.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                actuator.apply(new OutputEvent.advHdng(rh.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtMostOne && !inOpez
                    && odistCdynAboveMinSafe && odistCstcAboveMinSafe) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.Tick && inOpez) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && !inOpez && camGuard) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.Tick && !inOpez && !camGuard && hcmHorizGuard) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.Tick && !inOpez && !camGuard && vdistCstcWithinDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.Tick && !inOpez && !camGuard && hcmVertGuard) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && inOpez) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && !inOpez && camGuard) {
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.Tick && !inOpez && !camGuard && backToMomGuard) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && cdaAtLeastMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
