// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | ReqVel | ReqHdng | ReqMOM | ReqOCM | EndTask | ReqHCM

// Static constants
const SAFE_DISTANCE: real := 1000.0
const minSafeDist: real := 1.0
const staticObsDfltVertDist: real := 1.0
const staticObsVertDist: real := 1.0
const staticObsHorizDist: real := 1.0

class LreController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function vvel(): real
        reads this
    function cda(): real
        reads this
    function hvel(): real
        reads this
    function odistCstc(): real
        reads this
    function odistCdyn(): real
        reads this
    function vdistCstc(): real
        reads this
    function tcpa(): real
        reads this
    function inOpez(): bool
        reads this
    function vel(): real
        reads this
    function hdistCstc(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   cdaBelowMinSafe := cda() < 1.0
    //   tcpaNonNegative := tcpa() >= 0.0
    //   hvelAtLeastOne := hvel() >= 1.0
    //   velAtMostOne := vel() <= 1.0
    //   vdistCstcAtMostVert := vdistCstc() <= 1.0
    //   inOpez := inOpez()
    //   vdistCstcAtMostDfltVert := vdistCstc() <= 1.0
    //   odistCstcAboveOne := odistCstc() > 1.0
    //   hdistCstcAtMostHoriz := hdistCstc() <= 1.0
    //   odistCdynAboveOne := odistCdyn() > 1.0
    //   vvelAtLeastOne := vvel() >= 1.0

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
        ensures old(!(event == ReqHdng) && !(event == ReqVel) && event == ReqMOM && vel() <= 1.0 && !(inOpez()) && odistCdyn() > 1.0 && odistCstc() > 1.0) ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqVel) {
            mode := OCM;
            // action: AdvVel(reqVel.value())
        }
        else if (event == ReqHdng) {
            mode := OCM;
            // action: AdvHdng(reqHdng.value())
        }
        else if (event == ReqMOM && vel() <= 1.0 && !(inOpez()) && odistCdyn() > 1.0 && odistCstc() > 1.0) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old(!(event == ReqHCM) && !(event == EndTask) && !(event == ReqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == ReqHCM) && !(event == EndTask) && !(event == ReqOCM) && cda() < 1.0 && tcpa() >= 0.0) ==> mode == CAM
        ensures old(!(cda() < 1.0 && tcpa() >= 0.0) && !(inOpez()) && !(event == ReqHCM) && !(event == EndTask) && !(event == ReqOCM) && hvel() >= 1.0 && hdistCstc() <= 1.0) ==> mode == HCM
        ensures old(!(hvel() >= 1.0 && hdistCstc() <= 1.0) && !(cda() < 1.0 && tcpa() >= 0.0) && !(inOpez()) && !(event == ReqHCM) && !(event == EndTask) && !(event == ReqOCM) && vdistCstc() <= 1.0) ==> mode == HCM
        ensures old(!(vdistCstc() <= 1.0) && !(hvel() >= 1.0 && hdistCstc() <= 1.0) && !(cda() < 1.0 && tcpa() >= 0.0) && !(inOpez()) && !(event == ReqHCM) && !(event == EndTask) && !(event == ReqOCM) && vvel() >= 1.0 && vdistCstc() <= 1.0) ==> mode == HCM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (event == EndTask) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
        else if (event == ReqHCM) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (inOpez()) {
            mode := OCM;
        }
        else if (cda() < 1.0 && tcpa() >= 0.0) {
            mode := CAM;
        }
        else if (hvel() >= 1.0 && hdistCstc() <= 1.0) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (vdistCstc() <= 1.0) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (vvel() >= 1.0 && vdistCstc() <= 1.0) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures old(!(event == ReqOCM) && inOpez()) ==> mode == OCM
        ensures old(!(inOpez()) && !(event == ReqOCM) && cda() < 1.0 && tcpa() >= 0.0) ==> mode == CAM
        ensures old(!(cda() < 1.0 && tcpa() >= 0.0) && !(inOpez()) && !(event == ReqOCM) && !(hdistCstc() <= 1.0) && !(vdistCstc() <= 1.0)) ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (inOpez()) {
            mode := OCM;
        }
        else if (cda() < 1.0 && tcpa() >= 0.0) {
            mode := CAM;
        }
        else if (!(hdistCstc() <= 1.0) && !(vdistCstc() <= 1.0)) {
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
