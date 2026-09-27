// Auto-generated Dafny verification code from Java source
// Source: SRangerController.java (Spoon EMF model)

datatype Mode = MOVING | TURNING | FINAL

datatype InputEvent = NoEvent | EndTask | Obstacle | Tick

// Static constants
const TURN_VEL: real := 2.0
const NO_READING_DISTANCE: real := 1000000.0
const OBSTACLE_THRESHOLD: real := 0.5
const TURN_DURATION: real := 2.0
const MOVE_VEL: real := 1.0

class SRangerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var clockResetTime: real

    // Abstracted functions (from dependency objects -- uninterpreted)
    function distance(): real
        reads this
    function nowMs(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   obstacleDetected := distance() <= 0.5
    //   turnDurationElapsed := nowMs() - clockResetTime >= 2.0

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == MOVING
        ensures Valid()
    {
        mode := MOVING;
        clockResetTime := 0.0;
    }

    method transitionFromMOVING(event: InputEvent)
        requires mode == MOVING
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Obstacle && distance() <= 0.5) ==> mode == TURNING
        ensures Valid()
    {
        if (event == EndTask) {
            mode := FINAL;
            // action: Move(0.0)
        }
        else if (event == Obstacle && distance() <= 0.5) {
            mode := TURNING;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := MOVING;
        }
    }

    method transitionFromTURNING(event: InputEvent)
        requires mode == TURNING
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && nowMs() - clockResetTime >= 2.0) ==> mode == MOVING
        ensures Valid()
    {
        if (event == EndTask) {
            mode := FINAL;
            // action: Move(0.0)
        }
        else if (nowMs() - clockResetTime >= 2.0) {
            mode := MOVING;
            // action: Move(1.0)
        }
        else if (event == Tick) {
            mode := TURNING;
        }
    }

    method transitionFromFINAL(event: InputEvent)
        requires mode == FINAL
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Tick) {
            mode := FINAL;
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == MOVING) {
            transitionFromMOVING(event);
        }
        else if (mode == TURNING) {
            transitionFromTURNING(event);
        }
        else if (mode == FINAL) {
            transitionFromFINAL(event);
        }
    }
}
