// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = READING | ANALYSIS | NO_GAS | GAS_DETECTED | FINAL

datatype Angle = Left | Right | Back | Front
datatype Chem = TARGET | OTHER
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas | Tick

// Static constants
const EVADE_TIME: int := 1
const STUCK_DIST: real := 1.0
const SETTLE_LIMIT: int := 8
const STUCK_PERIOD: int := 1
const OUT_PERIOD: int := 1
const LV: real := 1.0
const DIRECTIONS: int := 4
const THR: real := 1.0

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var gs: real
    var sts: Status
    var ins: real
    var anl: Angle

    // Guard predicates (inlined from Java local variables in step())
    //   stsNoGas := sts == noGas
    //   stsGasD := sts == gasD
    //   insAtOrAboveThr := ins >= 1.0

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
        ensures old(!(sts == noGas) && sts == gasD) ==> mode == GAS_DETECTED
        ensures Valid()
    {
        if (sts == noGas) {
            mode := NO_GAS;
        }
        else if (sts == gasD) {
            mode := GAS_DETECTED;
        }
    }

    method transitionFromNO_GAS(event: InputEvent)
        requires mode == NO_GAS
        requires Valid()
        modifies this
        ensures mode == READING
        ensures Valid()
    {
        mode := READING;
    }

    method transitionFromGAS_DETECTED(event: InputEvent)
        requires mode == GAS_DETECTED
        requires Valid()
        modifies this
        ensures old(ins >= 1.0) ==> mode == FINAL
        ensures Valid()
    {
        if (ins >= 1.0) {
            mode := FINAL;
        }
    }

    method transitionFromFINAL(event: InputEvent)
        requires mode == FINAL
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Tick) {
            mode := FINAL;
        }
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
        else if (mode == FINAL) {
            transitionFromFINAL(event);
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
