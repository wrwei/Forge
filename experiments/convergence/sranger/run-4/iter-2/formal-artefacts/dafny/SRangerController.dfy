// Auto-generated Dafny verification code from Java source
// Source: SRangerController.java (Spoon EMF model)

datatype Mode = MOVING | TURNING | HALTED

datatype InputEvent = NoEvent | EndTask | Obstacle | Tick

// Static constants
const TURN_VEL: real := 1.0
const NO_READING_DEFAULT: real := 100.0
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
    function now(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   obstacleDetected := distance() <= 0.5
    //   turnDurationElapsed := now() - clockResetTime >= 2.0

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
        ensures event == Obstacle && distance() <= 0.5 ==> mode == TURNING
        ensures Valid()
    {
        if (event == EndTask) {
            mode := HALTED;
        }
        else if (event == Obstacle && distance() <= 0.5) {
            mode := TURNING;
        }
        else if (event == Tick) {
            mode := MOVING;
        }
    }

    method transitionFromTURNING(event: InputEvent)
        requires mode == TURNING
        requires Valid()
        modifies this
        ensures event == Tick && now() - clockResetTime >= 2.0 ==> mode == MOVING
        ensures Valid()
    {
        if (event == EndTask) {
            mode := HALTED;
        }
        else if (event == Tick && now() - clockResetTime >= 2.0) {
            mode := MOVING;
        }
        else if (event == Tick) {
            mode := TURNING;
        }
    }

    method transitionFromHALTED(event: InputEvent)
        requires mode == HALTED
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Tick) {
            mode := HALTED;
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
        else if (mode == HALTED) {
            transitionFromHALTED(event);
        }
    }
}
