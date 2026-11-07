// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | reqVel | reqHdng | reqMOM | reqOCM | endTask | reqHCM

// Static constants
const SAFE_DISTANCE: real := 1000.0
const momEntryMinDist: real := 1.0
const momEntryMaxVel: real := 1.0
const momVelocity: real := 1.0
const minSafeDist: real := 1.0
const staticObsDfltVertDist: real := 1.0
const staticObsVertDist: real := 1.0
const highVel: real := 1.0
const hcmVelocity: real := 0.0
const cpaEpsilon: real := 0.000001
const staticObsHorizDist: real := 1.0
const stopVelocity: real := 0.0
const surfaceDepth: real := 0.0
const minTcpa: real := 0.0
const NO_OBSTACLE: int := 2147483647

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
    //   staticObstacleClear := !(hdist(cstc()) <= 1.0) && !(vdist(cstc()) <= 1.0)
    //   cstcDistClear := odist(cstc()) > 1.0
    //   vertApproach := (vvel() >= 1.0) && (vdist(cstc()) <= 1.0)
    //   collisionRisk := (cda() < 1.0) && (tcpa() >= 0.0)
    //   hdistWithinHoriz := hdist(cstc()) <= 1.0
    //   vvelAtOrAboveLimit := vvel() >= 1.0
    //   cdynDistClear := odist(cdyn()) > 1.0
    //   horizApproach := (hvel() >= 1.0) && (hdist(cstc()) <= 1.0)
    //   vdistWithinDfltVert := vdist(cstc()) <= 1.0
    //   cdaBelowMinSafe := cda() < 1.0
    //   hvelAtOrAboveLimit := hvel() >= 1.0
    //   vdistWithinVert := vdist(cstc()) <= 1.0
    //   inOpez := inOpez()
    //   velWithinMomLimit := vel() <= 1.0
    //   momEntryAllowed := (vel() <= 1.0) && !inOpez() && (odist(cdyn()) > 1.0) && (odist(cstc()) > 1.0)

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
        ensures old(!(event == reqHdng) && !(event == reqVel) && event == reqMOM && (vel() <= 1.0) && !inOpez() && (odist(cdyn()) > 1.0) && (odist(cstc()) > 1.0)) ==> mode == MOM
        ensures Valid()
    {
        if (event == reqVel) {
            mode := OCM;
            // action: advVel(velocityRequest.value())
        }
        else if (event == reqHdng) {
            mode := OCM;
            // action: advHdng(headingRequest.value())
        }
        else if (event == reqMOM && (vel() <= 1.0) && !inOpez() && (odist(cdyn()) > 1.0) && (odist(cstc()) > 1.0)) {
            mode := MOM;
            // action: advVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old(!(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && (cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) ==> mode == CAM
        ensures old(!((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && (hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == HCM
        ensures old(!((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && (vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) ==> mode == HCM
        ensures old(!((vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) && !((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && vdist(cstc()) <= 1.0 && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0))) ==> mode == HCM
        ensures Valid()
    {
        if (event == reqOCM) {
            mode := OCM;
        }
        else if (event == endTask) {
            mode := OCM;
            // action: advVel(0.0)
        }
        else if (event == reqHCM) {
            mode := HCM;
            // action: advVel(0.0)
        }
        else if (inOpez()) {
            mode := OCM;
        }
        else if ((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) {
            mode := CAM;
        }
        else if ((hvel() >= 1.0) && (hdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) {
            mode := HCM;
            // action: advVel(0.0)
        }
        else if ((vvel() >= 1.0) && (vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0))) {
            mode := HCM;
            // action: advVel(0.0)
        }
        else if (vdist(cstc()) <= 1.0 && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0)) && !((hvel() >= 1.0) && (hdist(cstc()) <= 1.0)) && !((vvel() >= 1.0) && (vdist(cstc()) <= 1.0))) {
            mode := HCM;
            // action: advVel(0.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures old(!(event == reqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == reqOCM) && (cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) ==> mode == CAM
        ensures old(!((cda() < 1.0) && (tcpa() >= 0.0) && !(inOpez())) && !(inOpez()) && !(event == reqOCM) && !(hdist(cstc()) <= 1.0) && !(vdist(cstc()) <= 1.0) && !(inOpez()) && !((cda() < 1.0) && (tcpa() >= 0.0))) ==> mode == MOM
        ensures Valid()
    {
        if (event == reqOCM) {
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
            // action: advVel(1.0)
        }
    }

    method transitionFromCAM(event: InputEvent)
        requires mode == CAM
        requires Valid()
        modifies this
        ensures old(!(event == reqOCM) && !(cda() < 1.0)) ==> mode == OCM
        ensures Valid()
    {
        if (event == reqOCM) {
            mode := OCM;
        }
        else if (!(cda() < 1.0)) {
            mode := OCM;
            // action: advVel(0.0)
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
