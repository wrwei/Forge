package lre.controller;

import lre.actuator.Actuator;
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
 * Last Response Engine (LRE) controller.
 *
 * Single-method, mode-nested if-else state machine. The four modes are
 * OCM (initial), MOM, HCM, CAM (LRE-DM1).
 *
 * Each step:
 *   1. invokes the five operations (CalcVel, CalcCStc, CalcCDyn, CalcCPA, CheckOPEZ)
 *      in dependency order to refresh the controller state variables;
 *   2. evaluates named predicates from those variables;
 *   3. selects exactly one outgoing transition by the mode-nested if-else.
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

    // Controller state variables (LRE-Var1 .. LRE-Var8). Mirrored from operation outputs
    // so the ETL surfaces them as Ctrl_State fields with the canonical names.
    private boolean inOpez;

    @lre.annotation.RoboChartType("real")
    private double hvel;

    @lre.annotation.RoboChartType("real")
    private double vvel;

    @lre.annotation.RoboChartType("real")
    private double vel;

    @lre.annotation.RoboChartType("nat")
    private int cstc = -1;

    @lre.annotation.RoboChartType("nat")
    private int cdyn = -1;

    @lre.annotation.RoboChartType("real")
    private double cda;

    @lre.annotation.RoboChartType("real")
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

    public void step(InputEvent event) {
        // Refresh operation outputs in dependency order.
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // Mirror operation outputs into controller state variables.
        this.hvel = calcVel.hvel();
        this.vvel = calcVel.vvel();
        this.vel = calcVel.vel();
        this.cstc = calcCStc.cstc();
        this.cdyn = calcCDyn.cdyn();
        this.inOpez = checkOPEZ.inOpez();
        this.cda = calcCPA.cda();
        this.tcpa = calcCPA.tcpa();

        // --- Named boolean predicates (LRE-GP1 et al.) ---
        boolean velBelowOrAtOne = vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtOrAboveOne = hvel >= 1.0;
        boolean hdistCstcAtOrBelowStaticHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtOrBelowStaticDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAtOrAboveOne = vvel >= 1.0;
        boolean vdistCstcAtOrBelowStaticVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveStaticHoriz = sensor.hdist(cstc) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveStaticVert = sensor.vdist(cstc) > LreConstants.staticObsVertDist;
        boolean cdaAtOrAboveMinSafe = cda >= LreConstants.minSafeDist;

        // --- Mode-nested if-else ---
        if (currentMode == LreMode.OCM) {
            // OCM only responds to operator inputs (LRE-Beh19).
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqMOM
                    && velBelowOrAtOne
                    && !inOpez
                    && odistCdynAboveOne
                    && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // Event-triggered transitions first.
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // Autonomous transitions, ordered by safety priority.
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (hvelAtOrAboveOne && hdistCstcAtOrBelowStaticHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vdistCstcAtOrBelowStaticDfltVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vvelAtOrAboveOne && vdistCstcAtOrBelowStaticVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // Autonomous transitions.
            else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (hdistCstcAboveStaticHoriz && vdistCstcAboveStaticVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // Autonomous transition.
            else if (cdaAtOrAboveMinSafe) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
        }
    }
}
