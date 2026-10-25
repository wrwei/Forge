// Auto-generated Dafny verification code from Java source
// Source: SRangerController.java (Spoon EMF model)

datatype Mode = Moving | Turning | Final

datatype InputEvent = NoEvent | EndTask | Obstacle | Tick

// Static constants
const DEFAULT_DISTANCE: real := 1000000.0
const moveVel: real := 1.0
const turnVel: real := 2.0
const obstacleThreshold: real := 0.5
const turnDuration: real := 2.0

class SRangerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var clockResetTime: real

    // Abstracted functions (from dependency objects -- uninterpreted)
    function distance(): real
        reads this
    function nowSeconds(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   obstacleDetected := distance() <= 0.5
    //   turnDurationElapsed := nowSeconds() - clockResetTime >= 2.0

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
        clockResetTime := 0.0;
    }

    method transitionFromMoving(event: InputEvent)
        requires mode == Moving
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Obstacle) {
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
        ensures old(!(event == EndTask) && nowSeconds() - clockResetTime >= 2.0) ==> mode == Moving
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (nowSeconds() - clockResetTime >= 2.0) {
            mode := Moving;
            // action: Move(1.0)
        }
        else if (event == Tick) {
            mode := Turning;
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
        if (mode == Moving) {
            transitionFromMoving(event);
        }
        else if (mode == Turning) {
            transitionFromTurning(event);
        }
        else if (mode == Final) {
            transitionFromFinal(event);
        }
    }
}
