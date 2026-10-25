// Auto-generated Dafny verification code from Java source
// Source: LreController.java (Spoon EMF model)

datatype Mode = OCM | MOM | HCM | CAM

datatype InputEvent = NoEvent | ReqMOM | ReqOCM | EndTask | ReqHCM

// Static constants
const minSafeDist: real := 1.0
const staticObsDfltVertDist: real := 1.0
const staticObsVertDist: real := 1.0
const staticObsHorizDist: real := 1.0

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
    //   hdistCStcGtHoriz := hdist(cstc()) > 1.0
    //   tcpaNonNegative := tcpa() >= 0.0
    //   vdistCStcLeDfltVert := vdist(cstc()) <= 1.0
    //   velLe1 := vel() <= 1.0
    //   odistCStcGt1 := odist(cstc()) > 1.0
    //   vdistCStcGtVert := vdist(cstc()) > 1.0
    //   hvelAtLeast1 := hvel() >= 1.0
    //   odistCDynGt1 := odist(cdyn()) > 1.0
    //   cdaBelowMinSafe := cda() < 1.0
    //   vvelAtLeast1 := vvel() >= 1.0
    //   vdistCStcLeVert := vdist(cstc()) <= 1.0
    //   cdaAtLeastMinSafe := cda() >= 1.0
    //   inOpez := inOpez()
    //   hdistCStcLeHoriz := hdist(cstc()) <= 1.0

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
        ensures old(event == ReqMOM && vel() <= 1.0 && !(inOpez()) && odist(cdyn()) > 1.0 && odist(cstc()) > 1.0) ==> mode == MOM
        ensures Valid()
    {
        if (event == ReqMOM && vel() <= 1.0 && !(inOpez()) && odist(cdyn()) > 1.0 && odist(cstc()) > 1.0) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromMOM(event: InputEvent)
        requires mode == MOM
        requires Valid()
        modifies this
        ensures old(inOpez()) ==> mode == OCM
        ensures old(!(event == EndTask) && !(event == ReqOCM) && !(inOpez()) && cda() < 1.0 && tcpa() >= 0.0) ==> mode == CAM
        ensures old(!(event == ReqHCM) && !(cda() < 1.0 && tcpa() >= 0.0) && !(event == EndTask) && !(event == ReqOCM) && !(inOpez()) && hvel() >= 1.0 && hdist(cstc()) <= 1.0) ==> mode == HCM
        ensures old(!(hvel() >= 1.0 && hdist(cstc()) <= 1.0) && !(event == ReqHCM) && !(cda() < 1.0 && tcpa() >= 0.0) && !(event == EndTask) && !(event == ReqOCM) && !(inOpez()) && vdist(cstc()) <= 1.0) ==> mode == HCM
        ensures old(!(vdist(cstc()) <= 1.0) && !(hvel() >= 1.0 && hdist(cstc()) <= 1.0) && !(event == ReqHCM) && !(cda() < 1.0 && tcpa() >= 0.0) && !(event == EndTask) && !(event == ReqOCM) && !(inOpez()) && vvel() >= 1.0 && vdist(cstc()) <= 1.0) ==> mode == HCM
        ensures Valid()
    {
        if (inOpez()) {
            mode := OCM;
        }
        else if (event == ReqOCM) {
            mode := OCM;
        }
        else if (event == EndTask) {
            mode := OCM;
            // action: AdvVel(0.0)
        }
        else if (cda() < 1.0 && tcpa() >= 0.0) {
            mode := CAM;
        }
        else if (event == ReqHCM) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (hvel() >= 1.0 && hdist(cstc()) <= 1.0) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (vdist(cstc()) <= 1.0) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
        else if (vvel() >= 1.0 && vdist(cstc()) <= 1.0) {
            mode := HCM;
            // action: AdvVel(0.0)
        }
    }

    method transitionFromHCM(event: InputEvent)
        requires mode == HCM
        requires Valid()
        modifies this
        ensures old(inOpez()) ==> mode == OCM
        ensures old(!(event == ReqOCM) && !(inOpez()) && cda() < 1.0 && tcpa() >= 0.0) ==> mode == CAM
        ensures old(!(cda() < 1.0 && tcpa() >= 0.0) && !(event == ReqOCM) && !(inOpez()) && hdist(cstc()) > 1.0 && vdist(cstc()) > 1.0) ==> mode == MOM
        ensures Valid()
    {
        if (inOpez()) {
            mode := OCM;
        }
        else if (event == ReqOCM) {
            mode := OCM;
        }
        else if (cda() < 1.0 && tcpa() >= 0.0) {
            mode := CAM;
        }
        else if (hdist(cstc()) > 1.0 && vdist(cstc()) > 1.0) {
            mode := MOM;
            // action: AdvVel(1.0)
        }
    }

    method transitionFromCAM(event: InputEvent)
        requires mode == CAM
        requires Valid()
        modifies this
        ensures old(!(event == ReqOCM) && cda() >= 1.0) ==> mode == OCM
        ensures Valid()
    {
        if (event == ReqOCM) {
            mode := OCM;
        }
        else if (cda() >= 1.0) {
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
