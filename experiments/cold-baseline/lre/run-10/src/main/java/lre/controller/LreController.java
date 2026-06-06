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

public final class LreController {

    private LreMode currentMode = LreMode.OCM;

    private final Sensor sensor;
    private final Actuator actuator;
    private final CalcVel calcVel;
    private final CalcCStc calcCStc;
    private final CalcCDyn calcCDyn;
    private final CheckOPEZ checkOPEZ;
    private final CalcCPA calcCPA;

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
        // Invoke operations to refresh derived quantities each step.
        calcVel.compute();
        calcCStc.compute();
        calcCDyn.compute();
        checkOPEZ.compute();
        calcCPA.compute();

        // Named boolean predicates — declared BEFORE the if-else chain.
        boolean inOpez = checkOPEZ.inOpez();
        boolean velLeqOne = calcVel.vel() <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(calcCDyn.cdyn()) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(calcCStc.cstc()) > 1.0;
        boolean cdaBelowMinSafe = calcCPA.cda() < LreConstants.minSafeDist;
        boolean tcpaNonNegative = calcCPA.tcpa() >= 0.0;
        boolean hvelAboveOne = calcVel.hvel() >= 1.0;
        boolean hdistCstcLeqHoriz = sensor.hdist(calcCStc.cstc()) <= LreConstants.staticObsHorizDist;
        boolean vdistCstcLeqDflt = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsDfltVertDist;
        boolean vvelAboveOne = calcVel.vvel() >= 1.0;
        boolean vdistCstcLeqVert = sensor.vdist(calcCStc.cstc()) <= LreConstants.staticObsVertDist;
        boolean hdistCstcAboveHoriz = sensor.hdist(calcCStc.cstc()) > LreConstants.staticObsHorizDist;
        boolean vdistCstcAboveVert = sensor.vdist(calcCStc.cstc()) > LreConstants.staticObsVertDist;
        boolean cdaGeqMinSafe = calcCPA.cda() >= LreConstants.minSafeDist;

        if (currentMode == LreMode.OCM) {
            // OCM handles reqVel (passthrough), reqHdng (passthrough), reqMOM (transition).
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqMOM
                    && velLeqOne && !inOpez && odistCdynAboveOne && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // High-priority event-triggered transitions.
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.EndTask) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            } else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // Autonomous transitions ordered by priority.
            else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hvelAboveOne && hdistCstcLeqHoriz) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vdistCstcLeqDflt) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            } else if (vvelAboveOne && vdistCstcLeqVert) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // High-priority event-triggered transitions.
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // Autonomous transitions ordered by priority.
            else if (inOpez) {
                currentMode = LreMode.OCM;
            } else if (cdaBelowMinSafe && tcpaNonNegative) {
                currentMode = LreMode.CAM;
            } else if (hdistCstcAboveHoriz && vdistCstcAboveVert) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.CAM) {
            // High-priority event-triggered transitions.
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // Autonomous transitions ordered by priority.
            else if (cdaGeqMinSafe) {
                actuator.receive(new OutputEvent.AdvVel(0.0));
                currentMode = LreMode.OCM;
            }
        }
    }
}
