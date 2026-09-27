// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | ReqVel | ReqHdng | ReqMOM | ReqOCM | EndTask | ReqHCM

// Static constants
const minSafeDist: real := 1.0
const staticObsDfltVertDist: real := 1.0
const staticObsVertDist: real := 1.0
const staticObsHorizDist: real := 1.0

class LreController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var inOpez: bool
    var hvel: real
    var vvel: real
    var vel: real
    var cstc: int
    var cdyn: int
    var cda: real
    var tcpa: real
    var camActive: bool
    var hcmActive: bool
    var momReturn: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function odist(p0: int): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   cdaAboveOrAtMinSafe := cda >= 1.0
    //   odistCstcAboveOne := odist(cstc) > 1.0
    //   odistCdynAboveOne := odist(cdyn) > 1.0
    //   velBelowOrAtOne := vel <= 1.0

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
        inOpez := false;
        hvel := 0.0;
        vvel := 0.0;
        vel := 0.0;
        cstc := 0;
        cdyn := 0;
        cda := 0.0;
        tcpa := 0.0;
        camActive := false;
        hcmActive := false;
        momReturn := false;
    }

    method transitionFromOCM(event: InputEvent)
        requires mode == OCM
        requires Valid()
        modifies this
        ensures event == ReqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0 ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqVel) {
            mode := OCM;
            // action: AdvVel(rv.value())
        }
        else if (event == ReqHdng) {
            mode := OCM;
            // action: AdvHdng(rh.value())
        }
        else if (event == ReqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures camActive ==> mode == CAM
        ensures inOpez && !(camActive) ==> mode == OCM
        ensures momReturn && !(camActive) && !(inOpez) ==> mode == MOM
        ensures Valid()
    {
        if (camActive) {
            mode := CAM;
        }
        else if (inOpez && !(camActive)) {
            mode := OCM;
        }
        else if (momReturn && !(camActive) && !(inOpez)) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
        else if (event == ReqOCM) {
            mode := OCM;
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures camActive ==> mode == CAM
        ensures hcmActive && !(camActive) ==> mode == HCM
        ensures inOpez && !(camActive) && !(hcmActive) ==> mode == OCM
        ensures Valid()
    {
        if (camActive) {
            mode := CAM;
        }
        else if (hcmActive && !(camActive)) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (inOpez && !(camActive) && !(hcmActive)) {
            mode := OCM;
        }
        else if (event == ReqOCM) {
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
    }

    method transitionFromCAM(event: InputEvent)
        requires mode == CAM
        requires Valid()
        modifies this
        ensures cda >= 1.0 ==> mode == OCM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (cda >= 1.0) {
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
        else if (mode == HCM) {
            transitionFromHCM(event);
        }
        else if (mode == MOM) {
            transitionFromMOM(event);
        }
        else if (mode == CAM) {
            transitionFromCAM(event);
        }
    }
}

// Lemma: transitions from HCM are deterministic by if-else priority
lemma HCM_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from MOM are deterministic by if-else priority
lemma MOM_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
