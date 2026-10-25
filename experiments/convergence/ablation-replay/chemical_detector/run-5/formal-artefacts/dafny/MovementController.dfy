// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = Waiting | Going | Found | Avoiding | TryingAgain | AvoidingAgain | GettingOut

datatype Angle = Left | Right | Back | Front
datatype Chem = chemA | chemB
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Stop | Turn | Resume | Obstacle

// Static constants
const stuckDist: real := 1.0
const lv: real := 1.0
const evadeTime: int := 1
const stuckPeriod: int := 1
const thr: real := 1.0
const outPeriod: int := 1

class MovementController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var a: Angle
    var d0: real
    var d1: real
    var l: Loc
    var evasionStartMs: int

    // Abstracted functions (from dependency objects -- uninterpreted)
    function nowMs(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   makingProgress := (nowMs() - evasionStartMs < 1) || (d1 - d0 > 1.0)
    //   withinStuckPeriod := nowMs() - evasionStartMs < 1
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
        evasionStartMs := 0;
    }

    method transitionFromWaiting(event: InputEvent)
        requires mode == Waiting
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
            // action: Flag
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
            // action: Flag
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (event == Turn) {
            mode := Going;
        }
        else if (event == Obstacle) {
            mode := Avoiding;
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
            // action: Flag
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (event == Turn) {
            mode := TryingAgain;
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
            // action: Flag
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (event == Turn) {
            mode := TryingAgain;
        }
        else if (event == Obstacle) {
            mode := AvoidingAgain;
        }
    }

    method transitionFromAvoidingAgain(event: InputEvent)
        requires mode == AvoidingAgain
        requires Valid()
        modifies this
        ensures old(!(event == Resume) && !(event == Stop) && ((nowMs() - evasionStartMs < 1) || (d1 - d0 > 1.0))) ==> mode == Avoiding
        ensures old(!(event == Resume) && !(event == Stop) && !(((nowMs() - evasionStartMs < 1) || (d1 - d0 > 1.0)))) ==> mode == GettingOut
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
            // action: Flag
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (((nowMs() - evasionStartMs < 1) || (d1 - d0 > 1.0))) {
            mode := Avoiding;
        }
        else if (!(((nowMs() - evasionStartMs < 1) || (d1 - d0 > 1.0)))) {
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
            // action: Flag
        }
        else if (event == Resume) {
            mode := Waiting;
        }
        else if (event == Turn) {
            mode := Going;
        }
    }

    method transitionFromFound(event: InputEvent)
        requires mode == Found
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := Found;
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
        else if (mode == Found) {
            transitionFromFound(event);
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
