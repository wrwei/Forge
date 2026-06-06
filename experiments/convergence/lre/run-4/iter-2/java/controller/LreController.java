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
 * The Last Response Engine: a mode-nested if-else state machine over the
 * four operating modes OCM, MOM, HCM, CAM (LRE-ARCH1, LRE-ARCH2).
 * Initial mode is OCM (LRE-Beh1).
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

    private int cstc = -1;

    private int cdyn = -1;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    @RoboChartType("real")
    private double opVel;

    @RoboChartType("real")
    private double opHdng;

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.calcCPA = new CalcCPA(sensor, this.calcCDyn);
        this.checkOPEZ = new CheckOPEZ(sensor, this.calcCStc);
    }

    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all transitions (event-triggered and autonomous) for one
     * control cycle.
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
        boolean velAtMostNormal = this.vel <= 1.0;
        boolean odistCdynAboveMinSafe = sensor.odist(this.cdyn) > LreConstants.MIN_SAFE_DIST;
        boolean odistCstcAboveMinSafe = sensor.odist(this.cstc) > LreConstants.MIN_SAFE_DIST;
        boolean cdaBelowMinSafe = this.cda < LreConstants.MIN_SAFE_DIST;
        boolean tcpaNonNegative = this.tcpa >= 0.0;
        boolean collisionCourse = cdaBelowMinSafe && tcpaNonNegative;
        boolean hvelAtLeastNormal = this.hvel >= 1.0;
        boolean vvelAtLeastNormal = this.vvel >= 1.0;
        boolean hdistCstcWithinHoriz = sensor.hdist(this.cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean vdistCstcWithinVert = sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean vdistCstcWithinDfltVert = sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;

        // --- Mode-nested if-else state machine ---
        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.ReqVel) {
                // LRE-Beh2: pass operator velocity through to the autopilot.
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                this.opVel = rv.value();
                actuator.apply(new OutputEvent.AdvVel(this.opVel));
            } else if (event instanceof InputEvent.ReqHdng) {
                // LRE-Beh3: pass operator heading through to the autopilot.
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                this.opHdng = rh.value();
                actuator.apply(new OutputEvent.AdvHdng(this.opHdng));
            } else if (event instanceof InputEvent.ReqMOM && velAtMostNormal && !inOpez
                    && odistCdynAboveMinSafe && odistCstcAboveMinSafe) {
                // LRE-Beh4: OCM -> MOM; LRE-FR2 entry: advise 1 m/s.
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh6: MOM -> OCM on operator request.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                // LRE-Beh7: MOM -> OCM on end of task; advise 0 m/s.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.ReqHCM) {
                // LRE-Beh12: MOM -> HCM on operator request; LRE-FR3 entry: advise 0 m/s.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.Tick && inOpez) {
                // LRE-Beh5: MOM -> OCM when in the exclusion zone.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && !inOpez && collisionCourse) {
                // LRE-Beh8: MOM -> CAM on collision course.
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.Tick && !inOpez && !collisionCourse
                    && hvelAtLeastNormal && hdistCstcWithinHoriz) {
                // LRE-Beh9: MOM -> HCM, horizontal velocity near static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.Tick && !inOpez && !collisionCourse
                    && vdistCstcWithinDfltVert) {
                // LRE-Beh10: MOM -> HCM, vertically near static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            } else if (event instanceof InputEvent.Tick && !inOpez && !collisionCourse
                    && vvelAtLeastNormal && vdistCstcWithinVert) {
                // LRE-Beh11: MOM -> HCM, vertical velocity near static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh15: HCM -> OCM on operator request.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && inOpez) {
                // LRE-Beh16: HCM -> OCM when in the exclusion zone.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && !inOpez && collisionCourse) {
                // LRE-Beh14: HCM -> CAM on collision course.
                currentMode = LreMode.CAM;
            } else if (event instanceof InputEvent.Tick && !inOpez && !collisionCourse
                    && !hdistCstcWithinHoriz && !vdistCstcWithinVert) {
                // LRE-Beh13: HCM -> MOM when clear of the static obstacle;
                // LRE-FR2 entry: advise 1 m/s.
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.ReqOCM) {
                // LRE-Beh17: CAM -> OCM on operator request.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.Tick && !cdaBelowMinSafe) {
                // LRE-Beh18: CAM -> OCM when approach distance is safe; advise 0 m/s.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
