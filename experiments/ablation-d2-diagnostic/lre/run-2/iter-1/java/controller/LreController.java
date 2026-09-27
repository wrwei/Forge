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
 * The Last Response Engine: the AUV's safety controller (LRE-ARCH1, LRE-ARCH2).
 *
 * <p>The controller is a single-method, mode-nested state machine. Each step
 * runs the five derived-quantity operations, latches their results into the
 * controller's state variables, names every guard condition, and then selects
 * at most one transition out of the current mode.
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

    /** True when the AUV is inside the Object Proximity Exclusion Zone (LRE-Var1). */
    private boolean inOpez;

    /** Horizontal velocity of the AUV, in m/s (LRE-Var2). */
    @RoboChartType("real")
    private double hvel;

    /** Vertical velocity of the AUV, in m/s (LRE-Var3). */
    @RoboChartType("real")
    private double vvel;

    /** Overall velocity magnitude of the AUV, in m/s (LRE-Var4). */
    @RoboChartType("real")
    private double vel;

    /** Index of the closest static obstacle, -1 when there is none (LRE-Var5). */
    @RoboChartType("nat")
    private int cstc = -1;

    /** Index of the closest dynamic obstacle, -1 when there is none (LRE-Var6). */
    @RoboChartType("nat")
    private int cdyn = -1;

    /** Closest Distance of Approach, in metres (LRE-Var7). */
    @RoboChartType("real")
    private double cda;

    /** Time at Closest Point of Approach, in seconds (LRE-Var8). */
    @RoboChartType("real")
    private double tcpa;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
    }

    /** The mode the LRE is currently operating in. */
    public LreMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle. */
    public void step(InputEvent event) {
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

        // --- Named boolean predicates ---
        boolean velAtMostOne = vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafeDist = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeastOne = hvel >= 1.0;
        boolean vvelAtLeastOne = vvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtMostVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;

        // --- Mode-nested transition selection ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel request = (InputEvent.reqVel) event;
                actuator.apply(new OutputEvent.advVel(request.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng request = (InputEvent.reqHdng) event;
                actuator.apply(new OutputEvent.advHdng(request.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtMostOne && !inOpez
                    && odistCdynAboveOne && odistCstcAboveOne) {
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
            } else if (cdaBelowMinSafeDist && tcpaNonNegative) {
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
            } else if (cdaBelowMinSafeDist && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (!hdistCstcAtMostHoriz && !vdistCstcAtMostVert) {
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafeDist) {
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
