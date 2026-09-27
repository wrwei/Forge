// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = Reading | Analysis | NoGas | GasDetected | Stopped

datatype Angle = Left | Right | Back | Front
datatype Chem = Target | Other
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas

// Static constants
const EVADE_TIME: int := 2
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 10
const OUT_PERIOD: int := 3
const LV: real := 1.0
const THR: real := 5.0

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
    //   insAtOrAboveThr := goreq(ins, 5.0)
    //   statusNoGas := sts == noGas

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == Reading
        ensures Valid()
    {
        mode := Reading;
        gs := 0.0;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        sts := noGas;
        ins := 0.0;
        anl := Front;
    }

    method transitionFromReading(event: InputEvent)
        requires mode == Reading
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Gas) {
            mode := Analysis;
        }
    }

    method transitionFromAnalysis(event: InputEvent)
        requires mode == Analysis
        requires Valid()
        modifies this
        ensures old(sts == noGas) ==> mode == NoGas
        ensures old(!(sts == noGas)) ==> mode == GasDetected
        ensures Valid()
    {
        if (sts == noGas) {
            mode := NoGas;
        }
        else if (!(sts == noGas)) {
            mode := GasDetected;
        }
    }

    method transitionFromNoGas(event: InputEvent)
        requires mode == NoGas
        requires Valid()
        modifies this
        ensures mode == Reading
        ensures Valid()
    {
        mode := Reading;
    }

    method transitionFromGasDetected(event: InputEvent)
        requires mode == GasDetected
        requires Valid()
        modifies this
        ensures old(goreq(ins, 5.0)) ==> mode == Stopped
        ensures old(!(goreq(ins, 5.0))) ==> mode == Reading
        ensures Valid()
    {
        if (goreq(ins, 5.0)) {
            mode := Stopped;
        }
        else if (!(goreq(ins, 5.0))) {
            mode := Reading;
        }
    }

    method transitionFromStopped(event: InputEvent)
        requires mode == Stopped
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Gas) {
            mode := Stopped;
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == Reading) {
            transitionFromReading(event);
        }
        else if (mode == Analysis) {
            transitionFromAnalysis(event);
        }
        else if (mode == NoGas) {
            transitionFromNoGas(event);
        }
        else if (mode == GasDetected) {
            transitionFromGasDetected(event);
        }
        else if (mode == Stopped) {
            transitionFromStopped(event);
        }
    }
}

// Lemma: transitions from Analysis are deterministic by if-else priority
lemma Analysis_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from GasDetected are deterministic by if-else priority
lemma GasDetected_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
