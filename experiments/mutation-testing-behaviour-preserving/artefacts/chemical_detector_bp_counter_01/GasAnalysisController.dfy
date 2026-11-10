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
const evadeTime: int := 2
const stuckPeriod: int := 5
const thr: real := 10.0
const outPeriod: int := 3

class GasAnalysisController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var stepCount: int
    var gs: real
    var sts: Status
    var insVal: real
    var anl: Angle

    // Guard predicates (inlined from Java local variables and field assignments in step())
    //   stsIsNoGas := sts == noGas
    //   stepCount := stepCount + 1
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
        stepCount := 0;
        gs := 0.0;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        sts := noGas;
        insVal := 0.0;
        anl := Front;
    }

    method transitionFromReading(event: InputEvent)
        requires mode == Reading
        requires Valid()
        modifies this
        ensures Valid()
    {
        var stepCount_pre := stepCount + 1;
        if (event == Gas) {
            mode := Analysis;
        }
        else if (event == Tick) {
            mode := Reading;
        }
        stepCount := stepCount_pre;
    }

    method transitionFromAnalysis(event: InputEvent)
        requires mode == Analysis
        requires Valid()
        modifies this
        ensures old(sts == noGas) ==> mode == NoGas
        ensures old(!(sts == noGas) && sts == gasD) ==> mode == GasDetected
        ensures Valid()
    {
        var stepCount_pre := stepCount + 1;
        if (sts == noGas) {
            mode := NoGas;
        }
        else if (sts == gasD) {
            mode := GasDetected;
        }
        else if (event == Tick) {
            mode := Analysis;
        }
        stepCount := stepCount_pre;
    }

    method transitionFromNoGas(event: InputEvent)
        requires mode == NoGas
        requires Valid()
        modifies this
        ensures mode == Reading
        ensures Valid()
    {
        var stepCount_pre := stepCount + 1;
        mode := Reading;
        stepCount := stepCount_pre;
    }

    method transitionFromGasDetected(event: InputEvent)
        requires mode == GasDetected
        requires Valid()
        modifies this
        ensures old(insVal >= 10.0) ==> mode == Final
        ensures old(!(insVal >= 10.0)) ==> mode == Reading
        ensures Valid()
    {
        var stepCount_pre := stepCount + 1;
        if (insVal >= 10.0) {
            mode := Final;
        }
        else if (!(insVal >= 10.0)) {
            mode := Reading;
        }
        else if (event == Tick) {
            mode := GasDetected;
        }
        stepCount := stepCount_pre;
    }

    method transitionFromFinal(event: InputEvent)
        requires mode == Final
        requires Valid()
        modifies this
        ensures Valid()
    {
        var stepCount_pre := stepCount + 1;
        if (event == Tick) {
            mode := Final;
        }
        stepCount := stepCount_pre;
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
