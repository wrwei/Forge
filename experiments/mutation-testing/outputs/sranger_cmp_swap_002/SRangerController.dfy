// Auto-generated Dafny verification code from Java source
// Source: SRangerController.java (Spoon EMF model)

datatype Mode = Moving | Turning | Final

datatype InputEvent = NoEvent | EndTask | Obstacle | Tick

// Static constants
const moveVel: real := 1.0
const turnVel: real := 2.0
const obstacleThreshold: real := 0.5
const NO_READING_DEFAULT: real := 1000.0
const turnDurationMs: real := 2000.0
const turnDuration: real := 2.0

class SRangerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var turnDurationElapsed: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function distance(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   obstacleDetected := distance() <= 0.5

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == Moving
        ensures Valid()
    {
        mode := Moving;
        turnDurationElapsed := false;
    }

    method transitionFromMoving(event: InputEvent)
        requires mode == Moving
        requires Valid()
        modifies this
        ensures event == Obstacle && distance() <= 0.5 ==> mode == Turning
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Obstacle && distance() <= 0.5) {
            mode := Turning;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := Moving;
        }
    }

    method transitionFromTurning(event: InputEvent)
        requires mode == Turning
        requires Valid()
        modifies this
        ensures turnDurationElapsed ==> mode == Moving
        ensures Valid()
    {
        if (turnDurationElapsed) {
            mode := Moving;
            // action: Move(1.0)
        }
        else if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := Turning;
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == Moving) {
            transitionFromMoving(event);
        }
        else if (mode == Turning) {
            transitionFromTurning(event);
        }
    }
}
