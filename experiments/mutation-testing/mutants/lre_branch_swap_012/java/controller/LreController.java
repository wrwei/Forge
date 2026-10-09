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
 * The LRE (Last Response Engine) safety controller. Single-method,
 * mode-nested if-else state machine over {@link LreMode}. Each call to
 * {@link #step(InputEvent)} first refreshes the controller's derived state
 * variables from its operations, then selects exactly one outgoing
 * transition from the current mode.
 *
 * <p>The four modes are OCM (initial), MOM, HCM, and CAM. Within MOM and HCM
 * the controller enforces a safety-first priority hierarchy on autonomous
 * transitions: <b>CAM &gt; HCM-proximity &gt; OCM-return &gt; operator events</b>.
 * Each autonomous-transition guard explicitly conjoins the negations of all
 * higher-priority autonomous guards. This is required so that the Dafny
 * postconditions emitted by the M2T template (one ensures clause per behavioural
 * transition's guard) are mutually consistent: when two postcondition
 * antecedents would otherwise overlap and demand different target modes, the
 * negations make the lower-priority antecedent vacuous on the higher-priority
 * branch's path.
 *
 * <p>The compound predicates ({@link #camActive}, {@link #hcmActive},
 * {@link #momReturn}) are stored as instance <b>fields</b> rather than
 * method-local booleans on purpose. The M2T-Dafny template inlines local
 * boolean predicates into their underlying expression — which re-introduces
 * function calls (e.g. {@code hdist(cstc)}) into the emitted postconditions.
 * Functions declared {@code reads this} cannot be proven stable across
 * {@code mode := X} in Dafny's frame analysis, so postconditions containing
 * such function calls drift to "unknown" at method exit. By promoting the
 * compound predicates to fields, the emitted postconditions reference the
 * field names directly, and Dafny's framing tracks them precisely. See the
 * iteration-3 post_dafny_verify investigation for the full diagnosis.
 *
 * <p><b>Iter 4 state-space reduction:</b> the three HCM-source predicates
 * (LRE-Beh9 hvel-hdist, LRE-Beh10 vdist, LRE-Beh11 vvel-vdist) have been
 * merged into a single {@link #hcmActive} disjunction. They all map to the
 * same target mode (HCM) with the same entry action ({@code advVel(0)}), so
 * collapsing them is semantically transparent. This cuts the controller's
 * state-bearing boolean fields from 5 to 3, which reduces the FDR4 model's
 * LTS by ≈4× (from 2^5=32 to 2^3=8 boolean-product states). Iter 3 OOM'd
 * refines.exe at 117k processes; iter 4 is expected to halve and fit in
 * the 4 GB monitor budget. The disjunction is harmless for Dafny: each
 * source predicate still implies hcmActive, and the merged ensures clause
 * {@code hcmActive && !camActive ==> mode == HCM} verifies trivially.
 *
 * <p>The semantic interpretation of LRE-Beh19 used here: operator inputs are
 * overridden by LRE safety. Beh19 explicitly says velocity/heading operator
 * inputs are overridden in non-OCM modes; the natural extension applied here
 * is that mode-control operator events (reqOCM, endTask, reqHCM) are also
 * overridden when an autonomous safety condition is concurrently enabled.
 * Among autonomous conditions, CAM (collision-imminent dynamic obstacle) is
 * the most urgent response, then HCM (static-obstacle proximity), then the
 * OPEZ return-to-operator transition, matching the natural severity ordering
 * implied by the system description.
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

    // ---- LRE-Var1..8 — controller state variables refreshed each step ----

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

    // ---- Compound safety-priority predicate fields (see class javadoc) ----

    /** {@code cda < minSafeDist && tcpa >= 0} — collision-imminent (CAM trigger). */
    private boolean camActive;

    /**
     * Merged HCM-source disjunction: any of LRE-Beh9 (hvel at speed and close
     * horizontal static obstacle), LRE-Beh10 (close vertical static obstacle
     * at default threshold), or LRE-Beh11 (vvel at climb and close vertical
     * static obstacle). All three originally distinct predicates collapse here
     * because they share the same target mode (HCM) and the same entry action
     * (advVel(0)) — the resulting RoboChart transition is identical for all
     * three. This single boolean replaces three iter-3 fields, cutting the
     * FDR4 model's boolean-product state space by 4×.
     */
    private boolean hcmActive;

    /** LRE-Beh13: both horizontal and vertical static obstacle distances cleared. */
    private boolean momReturn;

    public LreController(
            Sensor sensor,
            Actuator actuator,
            CalcVel calcVel,
            CalcCStc calcCStc,
            CalcCDyn calcCDyn,
            CalcCPA calcCPA,
            CheckOPEZ checkOPEZ) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.calcVel = calcVel;
        this.calcCStc = calcCStc;
        this.calcCDyn = calcCDyn;
        this.calcCPA = calcCPA;
        this.checkOPEZ = checkOPEZ;
        this.inOpez = false;
        this.hvel = 0.0;
        this.vvel = 0.0;
        this.vel = 0.0;
        this.cstc = -1;
        this.cdyn = -1;
        this.cda = Double.MAX_VALUE;
        this.tcpa = -1.0;
        this.camActive = false;
        this.hcmActive = false;
        this.momReturn = false;
    }

    public LreMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        // --- Refresh derived quantities (operation dispatch order matters) ---
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

        // --- Compound predicate fields (computed here so Dafny treats them as
        //     stable instance fields; see class javadoc for rationale). ---
        this.camActive = this.cda < LreConstants.minSafeDist && this.tcpa >= 0.0;
        this.hcmActive = (this.hvel >= 1.0
                        && sensor.hdist(this.cstc) <= LreConstants.staticObsHorizDist)
                || sensor.vdist(this.cstc) <= LreConstants.staticObsDfltVertDist
                || (this.vvel >= 1.0
                        && sensor.vdist(this.cstc) <= LreConstants.staticObsVertDist);
        this.momReturn = sensor.hdist(this.cstc) > LreConstants.staticObsHorizDist
                && sensor.vdist(this.cstc) > LreConstants.staticObsVertDist;

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean velBelowOrAtOne = vel <= 1.0;
        boolean odistCdynAboveOne = sensor.odist(cdyn) > 1.0;
        boolean odistCstcAboveOne = sensor.odist(cstc) > 1.0;
        boolean cdaAboveOrAtMinSafe = cda >= LreConstants.minSafeDist;

        // --- Pure mode-nested if-else: one outer block per mode ---
        //
        // Within MOM and HCM, autonomous safety branches are ordered by descending
        // severity (CAM > HCM-proximity > OCM-via-OPEZ) AHEAD of all operator
        // events, and each lower-priority autonomous guard explicitly negates the
        // higher-priority ones. The negations are required for Dafny postcondition
        // consistency — see class javadoc. Within OCM and CAM the existing
        // event-driven ordering already verifies cleanly.
        if (currentMode == LreMode.OCM) {
            // LRE-Beh2 — reqVel passthrough, remain in OCM
            if (event instanceof InputEvent.ReqVel) {
                InputEvent.ReqVel rv = (InputEvent.ReqVel) event;
                actuator.receive(new OutputEvent.AdvVel(rv.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh3 — reqHdng passthrough, remain in OCM
            else if (event instanceof InputEvent.ReqHdng) {
                InputEvent.ReqHdng rh = (InputEvent.ReqHdng) event;
                actuator.receive(new OutputEvent.AdvHdng(rh.value()));
                currentMode = LreMode.OCM;
            }
            // LRE-Beh4 — reqMOM with guards → MOM
            else if (event instanceof InputEvent.ReqMOM
                    && velBelowOrAtOne
                    && !inOpez
                    && odistCdynAboveOne
                    && odistCstcAboveOne) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }

        } else if (currentMode == LreMode.MOM) {
            // LRE-Beh8 — collision risk → CAM (highest-priority autonomous transition)
            if (camActive) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh9 / LRE-Beh10 / LRE-Beh11 — any static-obstacle proximity → HCM
            // (CAM-excluded). The three originally separate predicates were collapsed
            // into hcmActive because they share the same target mode and entry action.
            else if (hcmActive && !camActive) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh5 — inOpez → OCM (autonomous), CAM/HCM-excluded
            else if (inOpez && !camActive && !hcmActive) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh6 — reqOCM → OCM (operator)
            else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh7 — endTask → OCM, advVel(0) (operator)
            else if (event instanceof InputEvent.EndTask) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
            // LRE-Beh12 — reqHCM → HCM (operator)
            else if (event instanceof InputEvent.ReqHCM) {
                currentMode = LreMode.HCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }

        } else if (currentMode == LreMode.HCM) {
            // LRE-Beh14 — collision risk → CAM (highest-priority autonomous)
            if (camActive) {
                currentMode = LreMode.CAM;
            }
            // LRE-Beh16 — inOpez → OCM (CAM-excluded)
            else if (momReturn && !camActive && !inOpez) {
                currentMode = LreMode.MOM;
                actuator.receive(new OutputEvent.AdvVel(1.0));
            }
            // LRE-Beh13 — clear of static obstacle → MOM (CAM-/OPEZ-excluded)
            else if (inOpez && !camActive) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh15 — reqOCM → OCM (operator)
            else if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }

        } else if (currentMode == LreMode.CAM) {
            // LRE-Beh17 — reqOCM → OCM
            if (event instanceof InputEvent.ReqOCM) {
                currentMode = LreMode.OCM;
            }
            // LRE-Beh18 — safe → OCM, advVel(0) (autonomous)
            else if (cdaAboveOrAtMinSafe) {
                currentMode = LreMode.OCM;
                actuator.receive(new OutputEvent.AdvVel(0.0));
            }
        }
    }
}
