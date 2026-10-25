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
const evadeTime: int := 2
const stuckPeriod: int := 5
const thr: real := 10.0
const outPeriod: int := 3

class MovementController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var a: Angle
    var l: Loc
    var d0: real
    var d1: real
    var odometer: real

    // Abstracted functions (from dependency objects -- uninterpreted)
    function since(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   makingProgress := (since() < 5) || (d1 - d0 > 1.0)
    //   evtIsTurn := event == Turn
    //   distanceProgress := d1 - d0 > 1.0
    //   evtIsStop := event == Stop
    //   evtIsResume := event == Resume
    //   stuck := (since() >= 5) && (d1 - d0 <= 1.0)
    //   withinStuckPeriod := since() < 5
    //   evtIsObstacle := event == Obstacle
    //   periodElapsed := since() >= 5
    //   noDistanceProgress := d1 - d0 <= 1.0

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
        a := Left;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        l := left;  // SUBSTITUTED: Java initialiser did not resolve to a literal; type default emitted
        d0 := 0.0;
        d1 := 0.0;
        odometer := 0.0;
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
    }
}
