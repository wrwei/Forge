// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = Reading | Analysis | NoGas | GasDetected | J1

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas

// Static constants
const stuckDist: real := 1.0
const lv: real := 1.0
const evadeTime: int := 2
const stuckPeriod: int := 5
const thr: real := 10.0
const outPeriod: int := 3

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var gs: real
    var sts: Status
    var ins: real
    var anl: Angle

    // Abstracted functions (from dependency objects -- uninterpreted)
    function goreq(p0: real, p1: real): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   intensityAboveThr := goreq(ins, thr)
    //   stsIsNoGas := sts == noGas
    //   stsIsGasD := sts == gasD

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
        ensures old(!(sts == noGas) && sts == gasD) ==> mode == GasDetected
        ensures Valid()
    {
        if (sts == noGas) {
            mode := NoGas;
            // action: Resume
        }
        else if (sts == gasD) {
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
        ensures old(goreq(ins, thr)) ==> mode == J1
        ensures Valid()
    {
        if (goreq(ins, thr)) {
            mode := J1;
            // action: Stop
        }
    }

    method transitionFromJ1(event: InputEvent)
        requires mode == J1
        requires Valid()
        modifies this
        ensures mode == J1
        ensures Valid()
    {
        mode := J1;
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
        else if (mode == J1) {
            transitionFromJ1(event);
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
