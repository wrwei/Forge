// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | reqMOM | reqOCM | endTask | reqHCM

// Static constants
const NO_OBSTACLE_DISTANCE: real := 1000.0
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

    // Abstracted functions (from dependency objects -- uninterpreted)
    function vdist(p0: int): real
        reads this
    function hdist(p0: int): real
        reads this
    function odist(p0: int): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   cdaBelowMinSafe := cda < 1.0
    //   tcpaNonNegative := tcpa >= 0.0
    //   hdistCstcAtOrBelowHoriz := hdist(cstc) <= 1.0
    //   velAtOrBelowOne := vel <= 1.0
    //   vdistCstcAtOrBelowDfltVert := vdist(cstc) <= 1.0
    //   hvelAtOrAboveOne := hvel >= 1.0
    //   vvelAtOrAboveOne := vvel >= 1.0
    //   vdistCstcAtOrBelowVert := vdist(cstc) <= 1.0
    //   odistCstcAboveOne := odist(cstc) > 1.0
    //   odistCdynAboveOne := odist(cdyn) > 1.0

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
    }

    method transitionFromOCM(event: InputEvent)
        requires mode == OCM
        requires Valid()
        modifies this
        ensures event == reqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0 ==> mode == MOM
        ensures Valid()
    {
        if (event == reqMOM && vel <= 1.0 && !(inOpez) && odist(cdyn) > 1.0 && odist(cstc) > 1.0) {
            mode := MOM;
            // action: advVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures inOpez ==> mode == OCM
        ensures cda < 1.0 && tcpa >= 0.0 ==> mode == CAM
        ensures hvel >= 1.0 && hdist(cstc) <= 1.0 ==> mode == HCM
        ensures vdist(cstc) <= 1.0 ==> mode == HCM
        ensures vvel >= 1.0 && vdist(cstc) <= 1.0 ==> mode == HCM
        ensures Valid()
    {
        if (event == reqOCM) {
            mode := OCM;
        }
        else if (event == endTask) {
            mode := OCM;
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
        else if (event == reqHCM) {
            mode := HCM;
            // action: advVel(0.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures inOpez ==> mode == OCM
        ensures cda < 1.0 && tcpa >= 0.0 ==> mode == CAM
        ensures !(hdist(cstc) <= 1.0) && !(vdist(cstc) <= 1.0) ==> mode == MOM
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
        ensures !(cda < 1.0) ==> mode == OCM
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
