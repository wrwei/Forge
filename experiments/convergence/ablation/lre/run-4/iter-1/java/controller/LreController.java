package lre.controller;

import lre.LreConstants;
import lre.actuator.Actuator;
import lre.annotation.RoboChartType;
import lre.event.InputEvent;
import lre.event.OutputEvent;
import lre.operation.CalcCDyn;
import lre.operation.CalcCPA;
import lre.operation.CalcCStc;
import lre.operation.CalcVel;
import lre.operation.CheckOPEZ;
import lre.sensor.Sensor;

/**
 * The Last Response Engine safety controller (LRE-ARCH1, LRE-ARCH2): a
 * single-method, mode-nested if-else state machine over the four operating
 * modes OCM, MOM, HCM, CAM. Each step it invokes the operations to refresh
 * the derived state variables, then evaluates transition conditions.
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

    public LreController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = new CalcVel(sensor);
        this.calcCStc = new CalcCStc(sensor);
        this.calcCDyn = new CalcCDyn(sensor);
        this.calcCPA = new CalcCPA(sensor);
        this.checkOPEZ = new CheckOPEZ(sensor);
    }

    /** The controller's current operating mode. */
    public LreMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates one control cycle: refreshes the derived state variables
     * via the operations, then evaluates all event-triggered and autonomous
     * transitions for the current mode.
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

        boolean velLeqNormal = vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaBelowMinSafe = cda < LreConstants.minSafeDist;
        boolean tcpaNonNegative = tcpa >= 0.0;
        boolean hvelAtOrAboveOne = hvel >= 1.0;
        boolean vvelAtOrAboveOne = vvel >= 1.0;
        boolean hdistCstcAtOrBelowHoriz = sensor.hdist(cstc) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcAtOrBelowVert = sensor.vdist(cstc) <= LreConstants.staticObsVertDist;
        boolean vdistCstcAtOrBelowDfltVert = sensor.vdist(cstc) <= LreConstants.staticObsDfltVertDist;

        if (currentMode == LreMode.OCM) {
            if (event instanceof InputEvent.reqVel) {
                // LRE-Beh2: pass operator velocity through to the autopilot.
                InputEvent.reqVel req = (InputEvent.reqVel) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(req.value()));
            } else if (event instanceof InputEvent.reqHdng) {
                // LRE-Beh3: pass operator heading through to the autopilot.
                InputEvent.reqHdng req = (InputEvent.reqHdng) event;
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advHdng(req.value()));
            } else if (event instanceof InputEvent.reqMOM && velLeqNormal && !inOpez
                    && odistCdynAboveOne && odistCstcAboveOne) {
                // LRE-Beh4: enter MOM; LRE-FR2 entry action advVel(1).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh6: operator reclaims control.
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.endTask) {
                // LRE-Beh7: end of task; advise velocity 0.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (event instanceof InputEvent.reqHCM) {
                // LRE-Beh12: operator requests HCM; LRE-FR3 entry advVel(0).
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (inOpez) {
                // LRE-Beh5: in OPEZ, return control to the operator.
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                // LRE-Beh8: collision course detected.
                currentMode = LreMode.CAM;
            } else if (hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz) {
                // LRE-Beh9: fast and horizontally close to a static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vdistCstcAtOrBelowDfltVert) {
                // LRE-Beh10: vertically close to a static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            } else if (vvelAtOrAboveOne && vdistCstcAtOrBelowVert) {
                // LRE-Beh11: climbing/diving fast near a static obstacle.
                currentMode = LreMode.HCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh15: operator reclaims control.
                currentMode = LreMode.OCM;
            } else if (inOpez) {
                // LRE-Beh16: in OPEZ, return control to the operator.
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                // LRE-Beh14: collision course detected.
                currentMode = LreMode.CAM;
            } else if (!hdistCstcAtOrBelowHoriz && !vdistCstcAtOrBelowVert) {
                // LRE-Beh13: clear of the static obstacle; LRE-FR2 entry advVel(1).
                currentMode = LreMode.MOM;
                actuator.apply(new OutputEvent.advVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            if (event instanceof InputEvent.reqOCM) {
                // LRE-Beh17: operator reclaims control.
                currentMode = LreMode.OCM;
            } else if (!cdaBelowMinSafe) {
                // LRE-Beh18: approach is safe again; advise velocity 0.
                currentMode = LreMode.OCM;
                actuator.apply(new OutputEvent.advVel(0.0));
            }
        }
    }
}
