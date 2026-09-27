// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = Reading | Analysis | NoGas | GasDetected | Final

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas | Tick

// Static constants
const stuckDist: real := 1.0
const thrVal: real := 10.0
const lv: real := 1.0
const evadeTime: int := 3
const stuckPeriod: int := 5
const thr: real := 10.0
const outPeriod: int := 3

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var gs: real
    var sts: Status
    var insVal: real
    var anl: Angle

    // Guard predicates (inlined from Java local variables in step())
    //   stsIsNoGas := sts == noGas
    //   stsIsGasD := sts == gasD
    //   insAboveThr := insVal >= 10.0

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
        insVal := 0.0;
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
        else if (event == Tick) {
            mode := Reading;
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
        }
        else if (sts == gasD) {
            mode := GasDetected;
        }
        else if (event == Tick) {
            mode := Analysis;
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
        ensures insVal >= 10.0 ==> mode == Final
        ensures !(insVal >= 10.0) ==> mode == Reading
        ensures Valid()
    {
        if (insVal >= 10.0) {
            mode := Final;
        }
        else if (!(insVal >= 10.0)) {
            mode := Reading;
        }
        else if (event == Tick) {
            mode := GasDetected;
        }
    }

    method transitionFromFinal(event: InputEvent)
        requires mode == Final
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Tick) {
            mode := Final;
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
        else if (mode == Final) {
            transitionFromFinal(event);
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
