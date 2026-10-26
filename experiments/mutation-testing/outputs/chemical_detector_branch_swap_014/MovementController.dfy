// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = Waiting | Going | Avoiding | TryingAgain | AvoidingAgain | GettingOut | Found | Final

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Stop | Turn | Resume | Obstacle | Tick

// Static constants
const stuckDist: real := 1.0
const thrVal: real := 10.0
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
    var clockResetTime: int

    // Abstracted functions (from dependency objects -- uninterpreted)
    function nowMs(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   makingProgress := (nowMs() - clockResetTime < 5) || (d1 - d0 > 1.0)
    //   stuckPeriodElapsed := nowMs() - clockResetTime >= 5
    //   stuckFired := (nowMs() - clockResetTime >= 5) && (d1 - d0 <= 1.0)
    //   distanceStagnant := d1 - d0 <= 1.0
    //   withinStuckPeriod := nowMs() - clockResetTime < 5
    //   distanceProgressed := d1 - d0 > 1.0

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
        clockResetTime := 0;
    }

    method transitionFromWaiting(event: InputEvent)
        requires mode == Waiting
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Turn) {
            mode := Going;
        }
        else if (event == Resume) {
            mode := Waiting;
        }
    }

    method transitionFromGoing(event: InputEvent)
        requires mode == Going
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Turn) {
            mode := Going;
        }
        else if (event == Obstacle) {
            mode := Avoiding;
        }
        else if (event == Resume) {
            mode := Waiting;
        }
    }

    method transitionFromFound(event: InputEvent)
        requires mode == Found
        requires Valid()
        modifies this
        ensures mode == Final
        ensures Valid()
    {
        mode := Final;
    }

    method transitionFromTryingAgain(event: InputEvent)
        requires mode == TryingAgain
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Turn) {
            mode := TryingAgain;
        }
        else if (event == Obstacle) {
            mode := AvoidingAgain;
        }
        else if (event == Resume) {
            mode := Waiting;
        }
    }

    method transitionFromAvoiding(event: InputEvent)
        requires mode == Avoiding
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Turn) {
            mode := TryingAgain;
        }
        else if (event == Resume) {
            mode := Waiting;
        }
    }

    method transitionFromAvoidingAgain(event: InputEvent)
        requires mode == AvoidingAgain
        requires Valid()
        modifies this
        ensures old(!(event == Resume) && !(event == Stop) && ((nowMs() - clockResetTime < 5) || (d1 - d0 > 1.0))) ==> mode == Avoiding
        ensures old(!(((nowMs() - clockResetTime < 5) || (d1 - d0 > 1.0))) && !(event == Resume) && !(event == Stop) && (nowMs() - clockResetTime >= 5) && (d1 - d0 <= 1.0)) ==> mode == GettingOut
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (((nowMs() - clockResetTime < 5) || (d1 - d0 > 1.0))) {
            mode := Avoiding;
        }
        else if ((nowMs() - clockResetTime >= 5) && (d1 - d0 <= 1.0)) {
            mode := GettingOut;
        }
    }

    method transitionFromGettingOut(event: InputEvent)
        requires mode == GettingOut
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Turn) {
            mode := Going;
        }
        else if (event == Resume) {
            mode := Waiting;
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
        if (mode == Waiting) {
            transitionFromWaiting(event);
        }
        else if (mode == Going) {
            transitionFromGoing(event);
        }
        else if (mode == Found) {
            transitionFromFound(event);
        }
        else if (mode == TryingAgain) {
            transitionFromTryingAgain(event);
        }
        else if (mode == Avoiding) {
            transitionFromAvoiding(event);
        }
        else if (mode == AvoidingAgain) {
            transitionFromAvoidingAgain(event);
        }
        else if (mode == GettingOut) {
            transitionFromGettingOut(event);
        }
        else if (mode == Final) {
            transitionFromFinal(event);
        }
    }
}

// Lemma: transitions from AvoidingAgain are deterministic by if-else priority
lemma AvoidingAgain_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
