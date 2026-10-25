// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = Waiting | Going | Found | Avoiding | TryingAgain | AvoidingAgain | GettingOut | Final

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent

// Static constants
const stuckDist: real := 1.0
const lv: real := 1.0
const evadeTime: int := 2
const stuckPeriod: int := 5
const thr: real := 10.0
const outPeriod: int := 3

class MovementController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var a: Angle
    var d0: real
    var d1: real
    var l: Loc
    var T: int
    var lastOdometer: real

    // Abstracted functions (from dependency objects -- uninterpreted)
    function nowMs(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   makingProgress := (nowMs() - T < 5) || (d1 - d0 > 1.0)
    //   sinceTBelowPeriod := nowMs() - T < 5
    //   stuck := !(nowMs() - T < 5) && !(d1 - d0 > 1.0)
    //   distAboveStuckDist := d1 - d0 > 1.0

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == Waiting
        ensures Valid()
    {
        mode := Waiting;
        a := Front;
        d0 := 0.0;
        d1 := 0.0;
        l := front;
        T := 0;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        lastOdometer := 0.0;
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
    }
}
