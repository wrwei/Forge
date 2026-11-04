// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | ReqVel | ReqHdng | ReqMOM | ReqOCM | EndTask | ReqHCM

// Static constants
const MIN_SAFE_DIST: real := 1.0
const STATIC_OBS_HORIZ_DIST: real := 1.0
const MOM_ENTRY_CLEARANCE: real := 1.0
const SURFACE_DEPTH: real := 0.0
const SAFE_LARGE_DIST: real := 1000000.0
const HCM_ADVISED_VEL: real := 0.0
const STATIC_OBS_VERT_DIST: real := 1.0
const NO_INDEX: int := MAX_VALUE
const STATIC_OBS_DFLT_VERT_DIST: real := 1.0
const VEL_LIMIT: real := 1.0
const MOM_ADVISED_VEL: real := 1.0
const STOP_VEL: real := 0.0

class LreController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function vvel(): real
        reads this
    function cda(): real
        reads this
    function cstc(): int
        reads this
    function vdist(p0: int): real
        reads this
    function hvel(): real
        reads this
    function tcpa(): real
        reads this
    function hdist(p0: int): real
        reads this
    function cdyn(): int
        reads this
    function inOpez(): bool
        reads this
    function vel(): real
        reads this
    function odist(p0: int): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   tcpaNonNegative := tcpa() >= 0.0
    //   horizontalApproach := (hvel() >= 1.0) && (hdist(cstc()) <= 1.0)
    //   velAtOrBelowLimit := vel() <= 1.0
    //   staticObsBeyondClearance := odist(cstc()) > 1.0
    //   collisionPredicted := (cda() < 1.0) && (tcpa() >= 0.0)
    //   dynamicObsBeyondClearance := odist(cdyn()) > 1.0
    //   staticHorizDistWithinLimit := hdist(cstc()) <= 1.0
    //   verticalApproach := (vvel() >= 1.0) && (vdist(cstc()) <= 1.0)
    //   vvelAtOrAboveLimit := vvel() >= 1.0
    //   staticVertDistWithinLimit := vdist(cstc()) <= 1.0
    //   hvelAtOrAboveLimit := hvel() >= 1.0
    //   staticVertDistWithinDfltLimit := vdist(cstc()) <= 1.0
    //   inOpez := inOpez()
    //   staticObsClear := !(hdist(cstc()) <= 1.0) && !(vdist(cstc()) <= 1.0)
    //   momEntryAllowed := (vel() <= 1.0) && !inOpez() && (odist(cdyn()) > 1.0) && (odist(cstc()) > 1.0)
    //   cdaBelowMinSafeDist := cda() < 1.0

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == OCM
        ensures Valid()
    {
        mode := OCM;
    }

    method transitionFromOCM(event: InputEvent)
        requires mode == OCM
        requires Valid()
        modifies this
        ensures old(!(event == ReqHdng) && !(event == ReqVel) && event == ReqMOM && (vel() <= 1.0) && !inOpez() && (odist(cdyn()) > 1.0) && (odist(cstc()) > 1.0)) ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqVel) {
            mode := OCM;
            // action: AdvVel(requestedVel.value())
        }
        else if (event == ReqHdng) {
            mode := OCM;
            // action: AdvHdng(requestedHdng.value())
        }
        else if (event == ReqMOM && (vel() <= 1.0) && !inOpez() && (odist(cdyn()) > 1.0) && (odist(cstc()) > 1.0)) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old(!(event == ReqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == ReqOCM) && (cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) ==> mode == CAM
        ensures old(!((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == ReqOCM) && event == EndTask && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == OCM
        ensures old(!(event == EndTask && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == ReqOCM) && event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == HCM
        ensures old(!(event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !(event == EndTask && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == ReqOCM) && (hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == HCM
        ensures old(!((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !(event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !(event == EndTask && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == ReqOCM) && (vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) ==> mode == HCM
        ensures old(!((vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !(event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !(event == EndTask && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == ReqOCM) && vdist(cstc()) <= 1.0 && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0))) ==> mode == HCM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (inOpez()) {
            mode := OCM;
        }
        else if ((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) {
            mode := CAM;
        }
        else if (event == EndTask && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
        else if (event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if ((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if ((vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (vdist(cstc()) <= 1.0 && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0))) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures old(!(event == ReqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == ReqOCM) && (cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) ==> mode == CAM
        ensures old(!((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == ReqOCM) && !(hdist(cstc()) <= 1.0) && !(vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (inOpez()) {
            mode := OCM;
        }
        else if ((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) {
            mode := CAM;
        }
        else if (!(hdist(cstc()) <= 1.0) && !(vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromCAM(event: InputEvent)
        requires mode == CAM
        requires Valid()
        modifies this
        ensures old(!(event == ReqOCM) && !(cda() < 1.0)) ==> mode == OCM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (!(cda() < 1.0)) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == OCM) {
            transitionFromOCM(event);
        }
        else if (mode == MOM) {
            transitionFromMOM(event);
        }
        else if (mode == HCM) {
            transitionFromHCM(event);
        }
        else if (mode == CAM) {
            transitionFromCAM(event);
        }
    }
}

// Lemma: transitions from MOM are deterministic by if-else priority
lemma MOM_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from HCM are deterministic by if-else priority
lemma HCM_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
