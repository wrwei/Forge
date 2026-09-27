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
 *
 * <p>A single-method, mode-nested if-else state machine over the four LRE
 * modes. Each step recomputes the derived quantities via the operation
 * classes, captures them into the controller's state variables, evaluates the
 * named guard predicates, and then selects at most one transition.</p>
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

    /** True when the AUV is inside an Object Proximity Exclusion Zone (LRE-Var1). */
    private boolean inOpez;

    /** Horizontal velocity, m/s (LRE-Var2). */
    @RoboChartType("real")
    private double hvel;

    /** Vertical velocity, m/s (LRE-Var3). */
    @RoboChartType("real")
    private double vvel;

    /** Overall velocity magnitude, m/s (LRE-Var4). */
    @RoboChartType("real")
    private double vel;

    /** Index of the closest static obstacle, -1 when none (LRE-Var5). */
    @RoboChartType("nat")
    private int cstc = -1;

    /** Index of the closest dynamic obstacle, -1 when none (LRE-Var6). */
    @RoboChartType("nat")
    private int cdyn = -1;

    /** Closest Distance of Approach, metres (LRE-Var7). */
    @RoboChartType("real")
    private double cda;

    /** Time at Closest Point of Approach, seconds (LRE-Var8). */
    @RoboChartType("real")
    private double tcpa;

    /** Latest operator-requested velocity, m/s. */
    @RoboChartType("real")
    private double reqVelValue;

    /** Latest operator-requested heading, degrees. */
    @RoboChartType("real")
    private double reqHdngValue;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
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

        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOPEZ.inOpez();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean velAtMost1 = vel <= 1.0;
        boolean odistCdynAbove1 = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAbove1 = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtLeast1 = hvel >= 1.0;
        boolean vvelAtLeast1 = vvel >= 1.0;
        boolean hdistCstcAtMostHoriz = sensor.hdist(cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAtMostVert = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcAtMostDfltVert = sensor.vdist(cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel reqVelEvent = (InputEvent.ReqVel) event;
                this.reqVelValue = reqVelEvent.value();
                actuator.apply(new OutputEvent.AdvVel(this.reqVelValue));
                this.currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng reqHdngEvent = (InputEvent.ReqHdng) event;
                this.reqHdngValue = reqHdngEvent.value();
                actuator.apply(new OutputEvent.AdvHdng(this.reqHdngValue));
                this.currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqMOM && velAtMost1 && !inOpez
                    && odistCdynAbove1 && odistCstcAbove1) {
                this.currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                this.currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                this.currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (inOpez) {
                this.currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                this.currentMode = LreMode.CAM;
            } else if (hvelAtLeast1 && hdistCstcAtMostHoriz) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vdistCstcAtMostDfltVert) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (vvelAtLeast1 && vdistCstcAtMostVert) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                this.currentMode = LreMode.OCM;
            } else if (inOpez) {
                this.currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                this.currentMode = LreMode.CAM;
            } else if (!hdistCstcAtMostHoriz && !vdistCstcAtMostVert) {
                this.currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                this.currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                this.currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
