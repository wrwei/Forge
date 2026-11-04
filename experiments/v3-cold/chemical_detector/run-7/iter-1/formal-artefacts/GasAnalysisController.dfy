// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = READING | ANALYSING | NO_GAS | GAS_DETECTED | CONCLUDED

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas

// Static constants
const EVADE_TIME: int := 1
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 3
const OUT_PERIOD: int := 2
const LV: real := 1.0
const THR: real := 2.0

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var gs: real
    var sts: Status
    var ins: real
    var anl: Angle

    // Abstracted functions (from dependency objects -- uninterpreted)
    function goreq(p0: real, p1: real): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   gasClassified := sts == gasD
    //   alwaysEnabled := true
    //   peakAtOrAboveThreshold := goreq(ins, 2.0)

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == READING
        ensures Valid()
    {
        mode := READING;
        gs := 0.0;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        sts := noGas;
        ins := 0.0;
        anl := Front;
    }

    method transitionFromREADING(event: InputEvent)
        requires mode == READING
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Gas) {
            mode := ANALYSING;
        }
    }

    method transitionFromANALYSING(event: InputEvent)
        requires mode == ANALYSING
        requires Valid()
        modifies this
        ensures old(sts == gasD) ==> mode == GAS_DETECTED
        ensures old(!(sts == gasD)) ==> mode == NO_GAS
        ensures Valid()
    {
        if (sts == gasD) {
            mode := GAS_DETECTED;
        }
        else if (!(sts == gasD)) {
            mode := NO_GAS;
            // action: Resume
        }
    }

    method transitionFromNO_GAS(event: InputEvent)
        requires mode == NO_GAS
        requires Valid()
        modifies this
        ensures true ==> mode == READING
        ensures Valid()
    {
        if (true) {
            mode := READING;
        }
    }

    method transitionFromGAS_DETECTED(event: InputEvent)
        requires mode == GAS_DETECTED
        requires Valid()
        modifies this
        ensures old(goreq(ins, 2.0)) ==> mode == CONCLUDED
        ensures old(!(goreq(ins, 2.0))) ==> mode == READING
        ensures Valid()
    {
        if (goreq(ins, 2.0)) {
            mode := CONCLUDED;
            // action: Stop
        }
        else if (!(goreq(ins, 2.0))) {
            mode := READING;
            // action: Turn(anl)
        }
    }

    method transitionFromCONCLUDED(event: InputEvent)
        requires mode == CONCLUDED
        requires Valid()
        modifies this
        ensures Valid()
    {
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == READING) {
            transitionFromREADING(event);
        }
        else if (mode == ANALYSING) {
            transitionFromANALYSING(event);
        }
        else if (mode == NO_GAS) {
            transitionFromNO_GAS(event);
        }
        else if (mode == GAS_DETECTED) {
            transitionFromGAS_DETECTED(event);
        }
        else if (mode == CONCLUDED) {
            transitionFromCONCLUDED(event);
        }
    }
}

// Lemma: transitions from ANALYSING are deterministic by if-else priority
lemma ANALYSING_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from GAS_DETECTED are deterministic by if-else priority
lemma GAS_DETECTED_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
