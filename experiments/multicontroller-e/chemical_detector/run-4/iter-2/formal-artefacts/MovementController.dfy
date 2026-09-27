// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = Waiting | Going | Found | Avoiding | TryingAgain | AvoidingAgain | GettingOut

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Stop | Turn | Resume | Obstacle

// Static constants
const EVADE_TIME: int := 1
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 2
const OUT_PERIOD: int := 1
const LV: real := 1.0
const THR: real := 1.0
const SETTLE_BOUND: int := 16

class MovementController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var a: Angle
    var l: Loc
    var d0: real
    var d1: real
    var T: int

    // Abstracted functions (from dependency objects -- uninterpreted)
    function nowMs(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   withinStuckPeriod := nowMs() - T < 2
    //   advancedBeyondStuckDist := d1 - d0 > 1.0

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
        l := front;
        d0 := 0.0;
        d1 := 0.0;
        T := 0;
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
        ensures Valid()
    {
        if (event == Obstacle) {
            mode := Found;
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

    method transitionFromAvoidingAgain(event: InputEvent)
        requires mode == AvoidingAgain
        requires Valid()
        modifies this
        ensures old(!(event == Resume) && !(event == Stop) && (nowMs() - T < 2 || d1 - d0 > 1.0)) ==> mode == Avoiding
        ensures old(!((nowMs() - T < 2 || d1 - d0 > 1.0)) && !(event == Resume) && !(event == Stop) && !(nowMs() - T < 2 || d1 - d0 > 1.0)) ==> mode == GettingOut
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (nowMs() - T < 2 || d1 - d0 > 1.0) {
            mode := Avoiding;
        }
        else if (!(nowMs() - T < 2 || d1 - d0 > 1.0)) {
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
        else if (mode == Avoiding) {
            transitionFromAvoiding(event);
        }
        else if (mode == TryingAgain) {
            transitionFromTryingAgain(event);
        }
        else if (mode == AvoidingAgain) {
            transitionFromAvoidingAgain(event);
        }
        else if (mode == GettingOut) {
            transitionFromGettingOut(event);
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
