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

/** The Last Response Engine: a mode-nested reactive safety controller for the AUV. */
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
    private int cstc;

    @RoboChartType("nat")
    private int cdyn;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public LreController(Sensor sensor, Actuator actuator, CalcVel calcVel,
            CalcCStc calcCStc, CalcCDyn calcCDyn, CalcCPA calcCPA, CheckOPEZ checkOPEZ) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = calcVel;
        this.calcCStc = calcCStc;
        this.calcCDyn = calcCDyn;
        this.calcCPA = calcCPA;
        this.checkOPEZ = checkOPEZ;
    }

    public LreMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle. */
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
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean momEntryAllowed = velAtMostOne && !inOpez && odistCdynAboveOne && odistCstcAboveOne;
        boolean cdaBelowMinSafeDist = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean camActive = cdaBelowMinSafeDist && tcpaNonNegative;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean hcmHorizVel = hvelAtLeastOne && hdistCstcAtMostHoriz;
        boolean hcmVertVel = vvelAtLeastOne && vdistCstcAtMostVert;
        boolean hcmCleared = !hdistCstcAtMostHoriz && !vdistCstcAtMostVert;

        // --- Mode-nested transitions ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel requested = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(requested.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng requested = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(requested.value()));
            } else if (event instanceof InputEvent.reqMOM && momEntryAllowed) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (camActive && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (hcmHorizVel && !inOpez && !camActive) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtMostDfltVert && !inOpez && !camActive && !hcmHorizVel) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (hcmVertVel && !inOpez && !camActive && !hcmHorizVel
                    && !vdistCstcAtMostDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (camActive && !inOpez) {
                currentMode = LreMode.CAM;
            } else if (hcmCleared && !inOpez && !camActive) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            }

        } else if (currentMode == LreMode.CAM) {
            if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            }
        }
    }
}
