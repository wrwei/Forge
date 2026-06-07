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
 * The LRE (Last Response Engine) safety controller for the AUV. Mediates
 * between the operator controller and the autopilot controller as a
 * single-method, mode-nested if-else state machine over four operating
 * modes (OCM, MOM, HCM, CAM).
 */
public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CalcCPA calcCPA;
    private final CheckOPEZ checkOPEZ;

    private boolean inOpez;

    @RoboChartType("real")
    private double hvel;

    @RoboChartType("real")
    private double vvel;

    @RoboChartType("real")
    private double vel;

    @RoboChartType("nat")
    private int cstc = -1;

    @RoboChartType("nat")
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
        this.calcCPA = new CalcCPA(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all event-triggered and autonomous transitions for the
     * current mode. Called once per control cycle.
     */
    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        calcCPA.compute();
        checkOPEZ.compute();

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();
        this.inOpez = checkOPEZ.inOpez();

        // --- Named boolean predicates ---
        boolean velAtMostOne = vel <= 1.0;
        boolean odistCdynAboveMinSafe = sensor.odist(cdyn) > LreConstants.minSafeDist;
        boolean odistCstcAboveMinSafe = sensor.odist(cstc) > LreConstants.minSafeDist;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean vdistCstcAtMostVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(cstc) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(cstc) > LreConstants.staticObsVertDist;
        boolean cdaAtLeastMinSafe = cda >= LreConstants.minSafeDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel req = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(req.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng req = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(req.value()));
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
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hvelAtLeastOne && hdistCstcAtMostHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtMostDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtLeastOne && vdistCstcAtMostVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (cdaAtLeastMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
