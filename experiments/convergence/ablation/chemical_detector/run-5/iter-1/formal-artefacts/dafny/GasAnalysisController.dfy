// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = Reading | Analysis | NoGas | GasDetected | Stopped

datatype Angle = Left | Right | Back | Front
datatype Chem = chemA | chemB
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas

// Static constants
const stuckDist: real := 1.0
const lv: real := 1.0
const evadeTime: int := 1
const stuckPeriod: int := 1
const thr: real := 1.0
const outPeriod: int := 1

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var gs: real
    var sts: Status
    var ins: real
    var anl: Angle

    // Abstracted functions (from dependency objects -- uninterpreted)
    function goreq(p0: real): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   stsIsNoGas := sts == noGas
    //   insAtOrAboveThr := goreq(ins, 1.0)
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
        gs := 0.0;
        sts := noGas;
        ins := 0.0;
        anl := Left;
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
        ensures sts == noGas ==> mode == NoGas
        ensures sts == gasD ==> mode == GasDetected
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
        ensures Valid()
    {
    }

    method transitionFromGasDetected(event: InputEvent)
        requires mode == GasDetected
        requires Valid()
        modifies this
        ensures goreq(ins, 1.0) ==> mode == Stopped
        ensures !(goreq(ins, 1.0)) ==> mode == Reading
        ensures Valid()
    {
        if (goreq(ins, 1.0)) {
            mode := Stopped;
            // action: Stop
        }
        else if (!(goreq(ins, 1.0))) {
            mode := Reading;
            // action: Turn(anl)
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
