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
 * The Last Response Engine: the AUV's safety controller
 * (LRE-ARCH1, LRE-ARCH2).
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

    @RoboChartType("real")
    private double reqVelValue;

    @RoboChartType("real")
    private double reqHdngValue;

    public LreController(Sensor sensor,
                         Actuator actuator,
                         CalcVel calcVel,
                         CalcCStc calcCStc,
                         CalcCDyn calcCDyn,
                         CheckOPEZ checkOPEZ,
                         CalcCPA calcCPA) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = calcVel;
        this.calcCStc = calcCStc;
        this.calcCDyn = calcCDyn;
        this.checkOPEZ = checkOPEZ;
        this.calcCPA = calcCPA;
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        hvel = calcVel.hvel();
        vvel = calcVel.vvel();
        vel = calcVel.vel();
        cstc = calcCStc.cstc();
        cdyn = calcCDyn.cdyn();
        inOpez = checkOPEZ.inOpez();
        cda = calcCPA.cda();
        tcpa = calcCPA.tcpa();

        // --- Named boolean predicates ---
        boolean velAtMostOne = vel <= 1.0;
        boolean odistCdynAboveMinSafe = sensor.odist(cdyn) > LreConstants.MIN_SAFE_DIST;
        boolean odistCstcAboveMinSafe = sensor.odist(cstc) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowMinSafe = cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAtMostVert = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;

        // --- Mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel reqVelEvent = (InputEvent.reqVel) event;
                reqVelValue = reqVelEvent.value();
                actuator.apply(new OutputEvent.advVel(reqVelValue));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng reqHdngEvent = (InputEvent.reqHdng) event;
                reqHdngValue = reqHdngEvent.value();
                actuator.apply(new OutputEvent.advHdng(reqHdngValue));
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
            } else if (!hdistCstcAtMostHoriz && !vdistCstcAtMostVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
