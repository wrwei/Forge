// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = Waiting | Going | Found | Avoiding | TryingAgain | AvoidingAgain | GettingOut

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | stop | resume | turn | obstacle

// Static constants
const EVADE_TIME: int := 1
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 2
const OUT_PERIOD: int := 1
const LV: real := 1.0
const THR: real := 1.0

class MovementController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var a: Angle
    var d0: real
    var d1: real
    var l: Loc
    var evadeStart: int

    // Abstracted functions (from dependency objects -- uninterpreted)
    function nowMs(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   evasionWithinStuckPeriod := nowMs() - evadeStart < 2
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
        d0 := 0.0;
        d1 := 0.0;
        l := front;
        evadeStart := 0;
    }

    method transitionFromWaiting(event: InputEvent)
        requires mode == Waiting
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
            // action: flag
        }
        else if (event == resume) {
            mode := Waiting;
        }
        else if (event == turn) {
            mode := Going;
        }
    }

    method transitionFromGoing(event: InputEvent)
        requires mode == Going
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
            // action: flag
        }
        else if (event == resume) {
            mode := Waiting;
        }
        else if (event == turn) {
            mode := Going;
        }
        else if (event == obstacle) {
            mode := Avoiding;
        }
    }

    method transitionFromFound(event: InputEvent)
        requires mode == Found
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
        }
        else if (event == obstacle) {
            mode := Found;
        }
    }

    method transitionFromAvoiding(event: InputEvent)
        requires mode == Avoiding
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
            // action: flag
        }
        else if (event == resume) {
            mode := Waiting;
        }
        else if (event == turn) {
            mode := TryingAgain;
        }
    }

    method transitionFromTryingAgain(event: InputEvent)
        requires mode == TryingAgain
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
            // action: flag
        }
        else if (event == resume) {
            mode := Waiting;
        }
        else if (event == turn) {
            mode := TryingAgain;
        }
        else if (event == obstacle) {
            mode := AvoidingAgain;
        }
    }

    method transitionFromAvoidingAgain(event: InputEvent)
        requires mode == AvoidingAgain
        requires Valid()
        modifies this
        ensures old(!(event == resume) && !(event == stop) && (nowMs() - evadeStart < 2 || d1 - d0 > 1.0)) ==> mode == Avoiding
        ensures old(!((nowMs() - evadeStart < 2 || d1 - d0 > 1.0)) && !(event == resume) && !(event == stop) && !(nowMs() - evadeStart < 2) && !(d1 - d0 > 1.0)) ==> mode == GettingOut
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
            // action: flag
        }
        else if (event == resume) {
            mode := Waiting;
        }
        else if (nowMs() - evadeStart < 2 || d1 - d0 > 1.0) {
            mode := Avoiding;
        }
        else if (!(nowMs() - evadeStart < 2) && !(d1 - d0 > 1.0)) {
            mode := GettingOut;
        }
    }

    method transitionFromGettingOut(event: InputEvent)
        requires mode == GettingOut
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == stop) {
            mode := Found;
            // action: flag
        }
        else if (event == resume) {
            mode := Waiting;
        }
        else if (event == turn) {
            mode := Going;
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
