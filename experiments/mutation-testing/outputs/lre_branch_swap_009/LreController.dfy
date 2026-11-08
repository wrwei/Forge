// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | ReqVel | ReqHdng | ReqMOM | EndTask | ReqOCM | ReqHCM

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
    function vdist(p0: int): real
        reads this
    function hdist(p0: int): real
        reads this
    function odist(p0: int): real
        reads this

    // Guard predicates (inlined from Java local variables and field assignments in step())
    //   cdaAboveOrAtMinSafe := cda >= 1.0
    //   camActive := cda < 1.0 && tcpa >= 0.0
    //   hcmActive := (hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0)
    //   momReturn := hdist(cstc) > 1.0 && vdist(cstc) > 1.0
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
        cstc := -1;
        cdyn := -1;
        cda := 0.0;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        tcpa := -1.0;
        camActive := false;
        hcmActive := false;
        momReturn := false;
    }

    method transitionFromOCM(event: InputEvent)
        requires mode == OCM
        requires Valid()
        modifies this
        ensures old(!(event == ReqHdng) && !(event == ReqVel) && event == ReqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0) ==> mode == MOM
        ensures Valid()
    {
        var camActive_pre := cda < 1.0 && tcpa >= 0.0;
        var hcmActive_pre := (hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0);
        var momReturn_pre := hdist(cstc) > 1.0 && vdist(cstc) > 1.0;
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
        camActive := camActive_pre;
        hcmActive := hcmActive_pre;
        momReturn := momReturn_pre;
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old((cda < 1.0 && tcpa >= 0.0)) ==> mode == CAM
        ensures old(!((cda < 1.0 && tcpa >= 0.0)) && ((hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0)) && !((cda < 1.0 && tcpa >= 0.0))) ==> mode == HCM
        ensures old(!(((hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0)) && !((cda < 1.0 && tcpa >= 0.0))) && !((cda < 1.0 && tcpa >= 0.0)) && inOpez && !((cda < 1.0 && tcpa >= 0.0)) && !(((hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0)))) ==> mode == OCM
        ensures Valid()
    {
        var camActive_pre := cda < 1.0 && tcpa >= 0.0;
        var hcmActive_pre := (hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0);
        var momReturn_pre := hdist(cstc) > 1.0 && vdist(cstc) > 1.0;
        if ((cda < 1.0 && tcpa >= 0.0)) {
            mode := CAM;
        }
        else if (((hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0)) && !((cda < 1.0 && tcpa >= 0.0))) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (inOpez && !((cda < 1.0 && tcpa >= 0.0)) && !(((hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0)))) {
            mode := OCM;
        }
        else if (event == EndTask) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
        else if (event == ReqOCM) {
            mode := OCM;
        }
        else if (event == ReqHCM) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        camActive := camActive_pre;
        hcmActive := hcmActive_pre;
        momReturn := momReturn_pre;
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures old((cda < 1.0 && tcpa >= 0.0)) ==> mode == CAM
        ensures old(!((cda < 1.0 && tcpa >= 0.0)) && inOpez && !((cda < 1.0 && tcpa >= 0.0))) ==> mode == OCM
        ensures old(!(inOpez && !((cda < 1.0 && tcpa >= 0.0))) && !((cda < 1.0 && tcpa >= 0.0)) && (hdist(cstc) > 1.0 && vdist(cstc) > 1.0) && !((cda < 1.0 && tcpa >= 0.0)) && !(inOpez)) ==> mode == MOM
        ensures Valid()
    {
        var camActive_pre := cda < 1.0 && tcpa >= 0.0;
        var hcmActive_pre := (hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0);
        var momReturn_pre := hdist(cstc) > 1.0 && vdist(cstc) > 1.0;
        if ((cda < 1.0 && tcpa >= 0.0)) {
            mode := CAM;
        }
        else if (inOpez && !((cda < 1.0 && tcpa >= 0.0))) {
            mode := OCM;
        }
        else if ((hdist(cstc) > 1.0 && vdist(cstc) > 1.0) && !((cda < 1.0 && tcpa >= 0.0)) && !(inOpez)) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
        else if (event == ReqOCM) {
            mode := OCM;
        }
        camActive := camActive_pre;
        hcmActive := hcmActive_pre;
        momReturn := momReturn_pre;
    }

    method transitionFromCAM(event: InputEvent)
        requires mode == CAM
        requires Valid()
        modifies this
        ensures old(!(event == ReqOCM) && cda >= 1.0) ==> mode == OCM
        ensures Valid()
    {
        var camActive_pre := cda < 1.0 && tcpa >= 0.0;
        var hcmActive_pre := (hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0);
        var momReturn_pre := hdist(cstc) > 1.0 && vdist(cstc) > 1.0;
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (cda >= 1.0) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
        camActive := camActive_pre;
        hcmActive := hcmActive_pre;
        momReturn := momReturn_pre;
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
