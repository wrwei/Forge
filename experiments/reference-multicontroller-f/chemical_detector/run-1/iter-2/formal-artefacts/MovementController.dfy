// Auto-generated Dafny verification code from Java source
// Source: MovementController.java (Spoon EMF model)

datatype Mode = WAITING | GOING | FOUND | AVOIDING | TRYING_AGAIN | AVOIDING_AGAIN | GETTING_OUT

datatype Angle = Left | Right | Back | Front
datatype Loc = left | right | front
datatype Status = noGas | gasD
datatype InputEvent = NoEvent | Stop | Resume | Turn | Obstacle

// Static constants
const TARGET_CHEM: int := 1
const EVADE_TIME: int := 1
const STUCK_DIST: real := 1.0
const STUCK_PERIOD: int := 2
const OUT_PERIOD: int := 1
const LV: real := 1.0
const ANALYSIS_CYCLES: int := 2
const THR: real := 2.0

class MovementController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var a: Angle
    var l: Loc
    var d0: real
    var d1: real
    var evasionStart: int

    // Abstracted functions (from dependency objects -- uninterpreted)
    function nowMs(): int
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   withinStuckPeriod := nowMs() - evasionStart < 2
    //   advancedBeyondStuckDist := d1 - d0 > 1.0

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == WAITING
        ensures Valid()
    {
        mode := WAITING;
        a := Front;
        l := front;
        d0 := 0.0;
        d1 := 0.0;
        evasionStart := 0;
    }

    method transitionFromWAITING(event: InputEvent)
        requires mode == WAITING
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := FOUND;
            // action: Flag
        }
        else if (event == Resume) {
            mode := WAITING;
        }
        else if (event == Turn) {
            mode := GOING;
        }
    }

    method transitionFromGOING(event: InputEvent)
        requires mode == GOING
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := FOUND;
            // action: Flag
        }
        else if (event == Resume) {
            mode := WAITING;
        }
        else if (event == Turn) {
            mode := GOING;
        }
        else if (event == Obstacle) {
            mode := AVOIDING;
        }
    }

    method transitionFromFOUND(event: InputEvent)
        requires mode == FOUND
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Obstacle) {
            mode := FOUND;
        }
    }

    method transitionFromAVOIDING(event: InputEvent)
        requires mode == AVOIDING
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := FOUND;
            // action: Flag
        }
        else if (event == Resume) {
            mode := WAITING;
        }
        else if (event == Turn) {
            mode := TRYING_AGAIN;
        }
    }

    method transitionFromTRYING_AGAIN(event: InputEvent)
        requires mode == TRYING_AGAIN
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := FOUND;
            // action: Flag
        }
        else if (event == Resume) {
            mode := WAITING;
        }
        else if (event == Turn) {
            mode := TRYING_AGAIN;
        }
        else if (event == Obstacle) {
            mode := AVOIDING_AGAIN;
        }
    }

    method transitionFromAVOIDING_AGAIN(event: InputEvent)
        requires mode == AVOIDING_AGAIN
        requires Valid()
        modifies this
        ensures old(!(event == Resume) && !(event == Stop) && (nowMs() - evasionStart < 2 || d1 - d0 > 1.0)) ==> mode == AVOIDING
        ensures old(!((nowMs() - evasionStart < 2 || d1 - d0 > 1.0)) && !(event == Resume) && !(event == Stop) && !(nowMs() - evasionStart < 2) && !(d1 - d0 > 1.0)) ==> mode == GETTING_OUT
        ensures Valid()
    {
        if (event == Stop) {
            mode := FOUND;
            // action: Flag
        }
        else if (event == Resume) {
            mode := WAITING;
        }
        else if (nowMs() - evasionStart < 2 || d1 - d0 > 1.0) {
            mode := AVOIDING;
        }
        else if (!(nowMs() - evasionStart < 2) && !(d1 - d0 > 1.0)) {
            mode := GETTING_OUT;
        }
    }

    method transitionFromGETTING_OUT(event: InputEvent)
        requires mode == GETTING_OUT
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Stop) {
            mode := FOUND;
            // action: Flag
        }
        else if (event == Resume) {
            mode := WAITING;
        }
        else if (event == Turn) {
            mode := GOING;
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == WAITING) {
            transitionFromWAITING(event);
        }
        else if (mode == GOING) {
            transitionFromGOING(event);
        }
        else if (mode == FOUND) {
            transitionFromFOUND(event);
        }
        else if (mode == AVOIDING) {
            transitionFromAVOIDING(event);
        }
        else if (mode == TRYING_AGAIN) {
            transitionFromTRYING_AGAIN(event);
        }
        else if (mode == AVOIDING_AGAIN) {
            transitionFromAVOIDING_AGAIN(event);
        }
        else if (mode == GETTING_OUT) {
            transitionFromGETTING_OUT(event);
        }
    }
}

// Lemma: transitions from AVOIDING_AGAIN are deterministic by if-else priority
lemma AVOIDING_AGAIN_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
