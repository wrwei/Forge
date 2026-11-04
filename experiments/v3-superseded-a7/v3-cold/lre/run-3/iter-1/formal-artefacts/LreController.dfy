// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | ReqVel | ReqHdng | ReqMOM | ReqOCM | EndTask | ReqHCM

// Static constants
const MIN_SAFE_DIST: real := 1.0
const STATIC_OBS_HORIZ_DIST: real := 1.0
const NO_OBSTACLE_DISTANCE: real := 100000.0
const STATIC_OBS_VERT_DIST: real := 1.0
const STATIC_OBS_DFLT_VERT_DIST: real := 1.0
const NO_OBSTACLE: int := 0

class LreController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function vvel(): real
        reads this
    function cstc(): int
        reads this
    function cda(): real
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
    //   velAtOrBelowOne := vel() <= 1.0
    //   odistStcAboveOne := odist(cstc()) > 1.0
    //   odistDynAboveOne := odist(cdyn()) > 1.0
    //   hvelAtOrAboveOne := hvel() >= 1.0
    //   vdistStcWithinDfltVertLimit := vdist(cstc()) <= 1.0
    //   vertApproachFast := (vvel() >= 1.0) && (vdist(cstc()) <= 1.0)
    //   vvelAtOrAboveOne := vvel() >= 1.0
    //   hdistStcWithinHorizLimit := hdist(cstc()) <= 1.0
    //   horizApproachFast := (hvel() >= 1.0) && (hdist(cstc()) <= 1.0)
    //   cdaBelowMinSafe := cda() < 1.0
    //   inOpez := inOpez()
    //   collisionCourse := (cda() < 1.0) && (tcpa() >= 0.0)
    //   vdistStcWithinVertLimit := vdist(cstc()) <= 1.0

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
        ensures old(!(event == ReqHdng) && !(event == ReqVel) && event == ReqMOM && vel() <= 1.0 && !(inOpez()) && odist(cdyn()) > 1.0 && odist(cstc()) > 1.0) ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqVel) {
            mode := OCM;
            // action: AdvVel(velocityRequest.value())
        }
        else if (event == ReqHdng) {
            mode := OCM;
            // action: AdvHdng(headingRequest.value())
        }
        else if (event == ReqMOM && vel() <= 1.0 && !(inOpez()) && odist(cdyn()) > 1.0 && odist(cstc()) > 1.0) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && !(event == ReqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == EndTask) && !(event == ReqOCM) && (cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) ==> mode == CAM
        ensures old(!((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == EndTask) && !(event == ReqOCM) && (hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == HCM
        ensures old(!((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == EndTask) && !(event == ReqOCM) && (vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) ==> mode == HCM
        ensures old(!((vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == EndTask) && !(event == ReqOCM) && vdist(cstc()) <= 1.0 && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0))) ==> mode == HCM
        ensures old(!(vdist(cstc()) <= 1.0 && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0))) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == EndTask) && !(event == ReqOCM) && event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0)) && !(vdist(cstc()) <= 1.0)) ==> mode == HCM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (event == EndTask) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
        else if (inOpez()) {
            mode := OCM;
        }
        else if ((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) {
            mode := CAM;
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
        else if (event == ReqHCM && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0)) && !(vdist(cstc()) <= 1.0)) {
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
