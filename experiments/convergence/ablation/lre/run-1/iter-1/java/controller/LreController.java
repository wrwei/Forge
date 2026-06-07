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
 * The Last Response Engine safety controller for the AUV (LRE-ARCH1,
 * LRE-ARCH2). A single-method, mode-nested if-else state machine over
 * the four operating modes OCM, MOM, HCM, CAM. Each step it invokes
 * the operations to refresh the derived state variables, then
 * evaluates transition conditions.
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

    /** Whether the AUV is in the Object Proximity Exclusion Zone (LRE-Var1). */
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
    /** Index of the closest static obstacle; -1 means none (LRE-Var5). */
    @RoboChartType("nat")
    private int cstc = -1;
    /** Index of the closest dynamic obstacle; -1 means none (LRE-Var6). */
    @RoboChartType("nat")
    private int cdyn = -1;
    /** Closest Distance of Approach, metres (LRE-Var7). */
    @RoboChartType("real")
    private double cda;
    /** Time at Closest Point of Approach, seconds (LRE-Var8). */
    @RoboChartType("real")
    private double tcpa;

    public LreController(Sensor sensor, Actuator actuator, CalcVel calcVel,
                         CalcCStc calcCStc, CalcCDyn calcCDyn,
                         CheckOPEZ checkOpez, CalcCPA calcCpa) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = calcVel;
        this.calcCStc = calcCStc;
        this.calcCDyn = calcCDyn;
        this.checkOpez = checkOpez;
        this.calcCpa = calcCpa;
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        // Invoke the operations before evaluating transition guards (LRE-ARCH2).
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOpez.compute();
        calcCpa.compute();
        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOpez.inOpez();
        this.cda = calcCpa.cda();
        this.tcpa = calcCpa.tcpa();

        // --- Named boolean predicates ---
        boolean velAtOrBelowOne = this.vel <= 1.0;
        boolean cdynDistAboveMinSafe = sensor.odist(this.cdyn) > LreConstants.MIN_SAFE_DIST;
        boolean cstcDistAboveMinSafe = sensor.odist(this.cstc) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowMinSafe = this.cda < LreConstants.MIN_SAFE_DIST;
        boolean cdaAtOrAboveMinSafe = this.cda >= LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = this.tcpa >= 0.0;
        boolean hvelAtOrAboveOne = this.hvel >= 1.0;
        boolean vvelAtOrAboveOne = this.vvel >= 1.0;
        boolean hdistCstcAtOrBelowHoriz = sensor.hdist(this.cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean hdistCstcAboveHoriz = sensor.hdist(this.cstc) > LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcAtOrBelowVert = sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcAboveVert = sensor.vdist(this.cstc) > LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcAtOrBelowDfltVert = sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;

        // --- Mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                // LRE-Beh2: pass operator velocity through to the autopilot.
                InputEvent.reqVel rv = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(rv.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                // LRE-Beh3: pass operator heading through to the autopilot.
                InputEvent.reqHdng rh = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(rh.value()));
            } else if (event instanceof InputEvent.reqMOM && velAtOrBelowOne && !inOpez
                    && cdynDistAboveMinSafe && cstcDistAboveMinSafe) {
                // LRE-Beh4: enter MOM; LRE-FR2 entry: advise 1 m/s.
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh6: operator reclaims control.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                // LRE-Beh7: end of task; advise 0 m/s.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                // LRE-Beh12: operator requests HCM; LRE-FR3 entry: advise 0 m/s.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                // LRE-Beh8: collision course detected.
                currentMode = LreMode.CAM;
            } else if (inOpez) {
                // LRE-Beh5: inside the exclusion zone.
                currentMode = LreMode.OCM;
            } else if (hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz) {
                // LRE-Beh9: fast horizontal approach to a static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtOrBelowDfltVert) {
                // LRE-Beh10: vertically close to a static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtOrAboveOne && vdistCstcAtOrBelowVert) {
                // LRE-Beh11: fast vertical approach to a static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh15: operator reclaims control.
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                // LRE-Beh14: collision course detected.
                currentMode = LreMode.CAM;
            } else if (inOpez) {
                // LRE-Beh16: inside the exclusion zone.
                currentMode = LreMode.OCM;
            } else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                // LRE-Beh13: clear of the static obstacle; LRE-FR2 entry: advise 1 m/s.
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }
        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh17: operator reclaims control.
                currentMode = LreMode.OCM;
            } else if (cdaAtOrAboveMinSafe) {
                // LRE-Beh18: approach is safe again; advise 0 m/s.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
