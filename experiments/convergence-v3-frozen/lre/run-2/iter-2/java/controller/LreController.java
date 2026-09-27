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
 * The Last Response Engine (LRE-ARCH1, LRE-ARCH2): the AUV's safety controller.
 * It mediates between the operator controller and the autopilot controller,
 * running one {@link #step(InputEvent)} per control cycle.
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

    /** LRE-Var1. */
    private boolean inOpez;

    /** LRE-Var2. */
    @RoboChartType("real")
    private double hvel;

    /** LRE-Var3. */
    @RoboChartType("real")
    private double vvel;

    /** LRE-Var4. */
    @RoboChartType("real")
    private double vel;

    /** LRE-Var5. */
    @RoboChartType("nat")
    private int cstc;

    /** LRE-Var6. */
    @RoboChartType("nat")
    private int cdyn;

    /** LRE-Var7. */
    @RoboChartType("real")
    private double cda;

    /** LRE-Var8. */
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

    /** The mode the LRE is currently in. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: refresh the derived quantities, then evaluate the
     * transitions leaving the current mode in priority order.
     */
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

        // --- Named boolean predicates (LRE-GP1 and the LRE-Beh guards) ---
        boolean momEntryClear = this.vel <= 1.0 && !this.inOpez
                && sensor.odist(this.cdyn) > 1.0 && sensor.odist(this.cstc) > 1.0;
        boolean camTrigger = this.cda < LreConstants.MIN_SAFE_DIST && this.tcpa >= 0.0;
        boolean camClear = this.cda >= LreConstants.MIN_SAFE_DIST;
        boolean hcmHorizTrigger = this.hvel >= 1.0
                && sensor.hdist(this.cstc) <= LreConstants.STATIC_OBS_HORIZ_DIST;
        boolean hcmVertVelTrigger = this.vvel >= 1.0
                && sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_VERT_DIST;
        boolean hcmDfltVertTrigger =
                sensor.vdist(this.cstc) <= LreConstants.STATIC_OBS_DFLT_VERT_DIST;
        boolean momReturnClear =
                sensor.hdist(this.cstc) > LreConstants.STATIC_OBS_HORIZ_DIST
                && sensor.vdist(this.cstc) > LreConstants.STATIC_OBS_VERT_DIST;

        // --- Mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2: pass the operator's requested velocity straight through.
            if (event instanceof InputEvent.reqVel) {
                InputEvent.reqVel requested = (InputEvent.reqVel) event;
                this.currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(requested.value()));
            // LRE-Beh3: pass the operator's requested heading straight through.
            } else if (event instanceof InputEvent.reqHdng) {
                InputEvent.reqHdng requested = (InputEvent.reqHdng) event;
                this.currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(requested.value()));
            // LRE-Beh4: take autonomous control when the operator asks and it is safe.
            } else if (event instanceof InputEvent.reqMOM && momEntryClear) {
                this.currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh6: the operator takes back control.
            if (event instanceof InputEvent.reqOCM) {
                this.currentMode = LreMode.OCM;
            // LRE-Beh7: the task is over; stop and hand back control.
            } else if (event instanceof InputEvent.endTask) {
                this.currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            // LRE-Beh12: the operator asks for high caution.
            } else if (event instanceof InputEvent.reqHCM) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            // LRE-Beh5: the AUV has entered an exclusion zone.
            } else if (inOpez) {
                this.currentMode = LreMode.OCM;
            // LRE-Beh8: a dynamic obstacle is on a collision course.
            } else if (!inOpez && camTrigger) {
                this.currentMode = LreMode.CAM;
            // LRE-Beh9: closing horizontally on a static obstacle.
            } else if (!inOpez && !camTrigger && hcmHorizTrigger) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            // LRE-Beh11: closing vertically on a static obstacle.
            } else if (!inOpez && !camTrigger && !hcmHorizTrigger && hcmVertVelTrigger) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            // LRE-Beh10: within the default vertical margin of a static obstacle.
            } else if (!inOpez && !camTrigger && !hcmHorizTrigger && !hcmVertVelTrigger
                    && hcmDfltVertTrigger) {
                this.currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh15: the operator takes back control.
            if (event instanceof InputEvent.reqOCM) {
                this.currentMode = LreMode.OCM;
            // LRE-Beh16: the AUV has entered an exclusion zone.
            } else if (inOpez) {
                this.currentMode = LreMode.OCM;
            // LRE-Beh14: a dynamic obstacle is on a collision course.
            } else if (!inOpez && camTrigger) {
                this.currentMode = LreMode.CAM;
            // LRE-Beh13: clear of the static obstacle again.
            } else if (!inOpez && !camTrigger && momReturnClear) {
                this.currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17: the operator takes back control.
            if (event instanceof InputEvent.reqOCM) {
                this.currentMode = LreMode.OCM;
            // LRE-Beh18: the collision risk has cleared.
            } else if (camClear) {
                this.currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
