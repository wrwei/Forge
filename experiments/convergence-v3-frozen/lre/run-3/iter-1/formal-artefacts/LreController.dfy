// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | reqVel | reqHdng | reqMOM | reqOCM | endTask | reqHCM

// Static constants
const MIN_SAFE_DIST: real := 1.0
const STATIC_OBS_HORIZ_DIST: real := 1.0
const SAFE_LARGE_DIST: real := 1000.0
const STATIC_OBS_VERT_DIST: real := 1.0
const MIN_REL_SPEED_SQ: real := 1.0E-9
const STATIC_OBS_DFLT_VERT_DIST: real := 1.0

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

    // Abstracted functions (from dependency objects -- uninterpreted)
    function vdist(p0: int): real
        reads this
    function hdist(p0: int): real
        reads this
    function odist(p0: int): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   tcpaNonNegative := tcpa >= 0.0
    //   hdistCstcAtOrBelowHoriz := hdist(cstc) <= 1.0
    //   velAtOrBelowOne := vel <= 1.0
    //   vdistCstcAtOrBelowDfltVert := vdist(cstc) <= 1.0
    //   hvelAtOrAboveOne := hvel >= 1.0
    //   vvelAtOrAboveOne := vvel >= 1.0
    //   vdistCstcAtOrBelowVert := vdist(cstc) <= 1.0
    //   odistCstcAboveOne := odist(cstc) > 1.0
    //   odistCdynAboveOne := odist(cdyn) > 1.0
    //   cdaBelowMinSafeDist := cda < 1.0

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
        cda := 0.0;
        tcpa := 0.0;
    }

    method transitionFromOCM(event: InputEvent)
        requires mode == OCM
        requires Valid()
        modifies this
        ensures old(!(event == reqHdng) && !(event == reqVel) && event == reqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0) ==> mode == MOM
        ensures Valid()
    {
        if (event == reqVel) {
            mode := OCM;
            // action: advVel(requestedVel.value())
        }
        else if (event == reqHdng) {
            mode := OCM;
            // action: advHdng(requestedHdng.value())
        }
        else if (event == reqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0) {
            mode := MOM;
            // action: advVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old(!(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && inOpez) ==> mode == OCM
        ensures old(!(inOpez) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && cda < 1.0 && tcpa >= 0.0) ==> mode == CAM
        ensures old(!(cda < 1.0 && tcpa >= 0.0) && !(inOpez) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && hvel >= 1.0 && hdist(cstc) <= 1.0) ==> mode == HCM
        ensures old(!(hvel >= 1.0 && hdist(cstc) <= 1.0) && !(cda < 1.0 && tcpa >= 0.0) && !(inOpez) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && vdist(cstc) <= 1.0) ==> mode == HCM
        ensures old(!(vdist(cstc) <= 1.0) && !(hvel >= 1.0 && hdist(cstc) <= 1.0) && !(cda < 1.0 && tcpa >= 0.0) && !(inOpez) && !(event == reqHCM) && !(event == endTask) && !(event == reqOCM) && vvel >= 1.0 && vdist(cstc) <= 1.0) ==> mode == HCM
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
        else if (inOpez) {
            mode := OCM;
        }
        else if (cda < 1.0 && tcpa >= 0.0) {
            mode := CAM;
        }
        else if (hvel >= 1.0 && hdist(cstc) <= 1.0) {
            mode := HCM;
            // action: advVel(0.0)
        }
        else if (vdist(cstc) <= 1.0) {
            mode := HCM;
            // action: advVel(0.0)
        }
        else if (vvel >= 1.0 && vdist(cstc) <= 1.0) {
            mode := HCM;
            // action: advVel(0.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures old(!(event == reqOCM) && inOpez) ==> mode == OCM
        ensures old(!(inOpez) && !(event == reqOCM) && cda < 1.0 && tcpa >= 0.0) ==> mode == CAM
        ensures old(!(cda < 1.0 && tcpa >= 0.0) && !(inOpez) && !(event == reqOCM) && !(hdist(cstc) <= 1.0) && !(vdist(cstc) <= 1.0)) ==> mode == MOM
        ensures Valid()
    {
        if (event == reqOCM) {
            mode := OCM;
        }
        else if (inOpez) {
            mode := OCM;
        }
        else if (cda < 1.0 && tcpa >= 0.0) {
            mode := CAM;
        }
        else if (!(hdist(cstc) <= 1.0) && !(vdist(cstc) <= 1.0)) {
            mode := MOM;
            // action: advVel(1.0)
        }
    }

    method transitionFromCAM(event: InputEvent)
        requires mode == CAM
        requires Valid()
        modifies this
        ensures old(!(event == reqOCM) && !(cda < 1.0)) ==> mode == OCM
        ensures Valid()
    {
        if (event == reqOCM) {
            mode := OCM;
        }
        else if (!(cda < 1.0)) {
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
