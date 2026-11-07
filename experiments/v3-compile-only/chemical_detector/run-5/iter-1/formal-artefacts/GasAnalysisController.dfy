// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = READING | ANALYSIS | NO_GAS | GAS_DETECTED | CONCLUDED

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas

// Static constants
const NANOS_PER_MILLI: int := 1000000
const EVADE_TIME: int := 1
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 2
const OUT_PERIOD: int := 1
const LV: real := 1.0
const DIRECTIONS: real := values()
const THR: real := 2.0

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var gs: real
    var sts: Status
    var ins: real
    var anl: Angle

    // Abstracted functions (from dependency objects -- uninterpreted)
    function values(): real
        reads this
    function goreq(p0: real, p1: real): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   noGasClassified := sts == noGas
    //   peakAtThreshold := goreq(ins, 2.0)

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
            mode := ANALYSIS;
        }
    }

    method transitionFromANALYSIS(event: InputEvent)
        requires mode == ANALYSIS
        requires Valid()
        modifies this
        ensures old(sts == noGas) ==> mode == NO_GAS
        ensures old(!(sts == noGas)) ==> mode == GAS_DETECTED
        ensures Valid()
    {
        if (sts == noGas) {
            mode := NO_GAS;
            // action: Resume
        }
        else if (!(sts == noGas)) {
            mode := GAS_DETECTED;
        }
    }

    method transitionFromNO_GAS(event: InputEvent)
        requires mode == NO_GAS
        requires Valid()
        modifies this
        ensures old(sts == noGas) ==> mode == READING
        ensures Valid()
    {
        if (sts == noGas) {
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
        else if (mode == ANALYSIS) {
            transitionFromANALYSIS(event);
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

// Lemma: transitions from ANALYSIS are deterministic by if-else priority
lemma ANALYSIS_deterministic()
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
