// Auto-generated Dafny verification code from Java source
// Source: SynController.java (Spoon EMF model)

datatype Mode = M1 | M2 | M3 | Final

datatype InputEvent = NoEvent | EndTask | Advance | Retreat | Tick

// Static constants
const threshold3: real := 1.25
const moveVel: real := 1.0
const threshold1: real := 0.75
const threshold2: real := 1.0

class SynController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function reading1(): real
        reads this
    function reading2(): real
        reads this
    function reading3(): real
        reads this

    // Guard predicates (inlined from Java local variables and field assignments in step())
    //   below1 := reading1() <= 0.75
    //   below2 := reading2() <= 1.0
    //   below3 := reading3() <= 1.25

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == M1
        ensures Valid()
    {
        mode := M1;
    }

    method transitionFromM1(event: InputEvent)
        requires mode == M1
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading1() <= 0.75) ==> mode == M2
        ensures old(!(event == Advance && reading1() <= 0.75) && !(event == EndTask) && event == Retreat && !(reading1() <= 0.75)) ==> mode == M1
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading1() <= 0.75) {
            mode := M2;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading1() <= 0.75)) {
            mode := M1;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M1;
        }
    }

    method transitionFromM2(event: InputEvent)
        requires mode == M2
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading2() <= 1.0) ==> mode == M3
        ensures old(!(event == Advance && reading2() <= 1.0) && !(event == EndTask) && event == Retreat && !(reading2() <= 1.0)) ==> mode == M1
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading2() <= 1.0) {
            mode := M3;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading2() <= 1.0)) {
            mode := M1;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M2;
        }
    }

    method transitionFromM3(event: InputEvent)
        requires mode == M3
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading3() <= 1.25) ==> mode == M1
        ensures old(!(event == Advance && reading3() <= 1.25) && !(event == EndTask) && event == Retreat && !(reading3() <= 1.25)) ==> mode == M2
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading3() <= 1.25) {
            mode := M1;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading3() <= 1.25)) {
            mode := M2;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M3;
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
        if (mode == M1) {
            transitionFromM1(event);
        }
        else if (mode == M2) {
            transitionFromM2(event);
        }
        else if (mode == M3) {
            transitionFromM3(event);
        }
        else if (mode == Final) {
            transitionFromFinal(event);
        }
    }
}
