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
 * The Last Response Engine: safety controller for the AUV (LRE-ARCH1,
 * LRE-ARCH2). A single-method, mode-nested if-else state machine over the
 * four operating modes OCM, MOM, HCM, CAM. Initial mode is OCM (LRE-Beh1).
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
    @RoboChartType("real")
    private double opVel;
    @RoboChartType("real")
    private double opHdng;

    public LreController(Sensor sensor, Actuator actuator, CalcVel calcVel,
            CalcCStc calcCStc, CalcCDyn calcCDyn, CalcCPA calcCPA,
            CheckOPEZ checkOPEZ) {
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

    public void step(InputEvent event) {
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        calcCPA.compute();
        checkOPEZ.compute();

        // --- Named boolean predicates ---
        boolean inOpez = checkOPEZ.inOpez();
        boolean velLeqOne = calcVel.vel() <= 1.0;
        boolean hvelGeqOne = calcVel.hvel() >= 1.0;
        boolean vvelGeqOne = calcVel.vvel() >= 1.0;
        boolean odistCdynAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean hdistCstcLeqHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCstcLeqVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;
        boolean vdistCstcLeqDfltVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean cdaGeqMinSafe = calcCPA.cda() >= LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean collisionRisk = cdaBelowMinSafe && tcpaNonNegative;

        // --- Mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                opVel = rv.value();
                actuator.apply(new OutputEvent.advVel(opVel));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                opHdng = rh.value();
                actuator.apply(new OutputEvent.advHdng(opHdng));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.reqMOM && velLeqOne && !inOpez
                    && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (collisionRisk) {
                currentMode = LreMode.CAM;
            } else if (!collisionRisk && inOpez) {
                currentMode = LreMode.OCM;
            } else if (!collisionRisk && !inOpez && hvelGeqOne && hdistCstcLeqHoriz) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (!collisionRisk && !inOpez && vdistCstcLeqDfltVert) {
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (!collisionRisk && !inOpez && vvelGeqOne && vdistCstcLeqVert) {
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
            if (collisionRisk) {
                currentMode = LreMode.CAM;
            } else if (!collisionRisk && inOpez) {
                currentMode = LreMode.OCM;
            } else if (!collisionRisk && !inOpez && hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            } else if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (cdaGeqMinSafe) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
