// Auto-generated Dafny verification code from Java source
// Source: GasAnalysisController.java (Spoon EMF model)

datatype Mode = Reading | Analysis | NoGas | GasDetected | Final

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Gas

// Static constants
const TARGET: real := 1
const EVADE_TIME: int := 5
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 10
const OUT_PERIOD: int := 8
const LV: real := 1.0
const THR: real := 80.0

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
    //   stsIsGasD := sts == gasD
    //   insAboveThr := goreq(ins, 80.0)

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
        ensures old(sts == gasD) ==> mode == GasDetected
        ensures Valid()
    {
        if (sts == gasD) {
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
        ensures old(goreq(ins, 80.0)) ==> mode == Final
        ensures Valid()
    {
        if (goreq(ins, 80.0)) {
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
    }
}
