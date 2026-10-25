// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = Waiting | Going | Avoiding | TryingAgain | AvoidingAgain | GettingOut | Found | Final

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent

// Static constants
const stuckDist: real := 1.0
const lv: real := 1.0
const evadeTime: int := 5
const stuckPeriod: int := 10
const thr: real := 50.0
const outPeriod: int := 5

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
    //   makingProgressByDist := d1 - d0 > 1.0
    //   makingProgress := (nowMs() - T < 10) || (d1 - d0 > 1.0)
    //   makingProgressByTime := nowMs() - T < 10
    //   stuck := (nowMs() - T >= 10) && (d1 - d0 <= 1.0)
    //   stuckByTime := nowMs() - T >= 10
    //   obstacleLeft := l == left
    //   stuckByDist := d1 - d0 <= 1.0
    //   obstacleRight := l == right
    //   obstacleFront := l == front

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
        T := 0;
        lastOdometer := 0.0;
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
    }
}
