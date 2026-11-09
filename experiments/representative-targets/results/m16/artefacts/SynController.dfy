// Auto-generated Dafny verification code from Java source
// Source: SynController.java (Spoon EMF model)

datatype Mode = M1 | M2 | M3 | M4 | M5 | M6 | M7 | M8 | M9 | M10 | M11 | M12 | M13 | M14 | M15 | Final

datatype InputEvent = NoEvent | EndTask | Advance | Retreat | Tick

// Static constants
const threshold5: real := 1.75
const threshold10: real := 3.0
const threshold6: real := 2.0
const threshold11: real := 3.25
const threshold3: real := 1.25
const moveVel: real := 1.0
const threshold4: real := 1.5
const threshold9: real := 2.75
const threshold14: real := 4.0
const threshold15: real := 4.25
const threshold7: real := 2.25
const threshold12: real := 3.5
const threshold8: real := 2.5
const threshold13: real := 3.75
const threshold1: real := 0.75
const threshold2: real := 1.0

class SynController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function reading11(): real
        reads this
    function reading12(): real
        reads this
    function reading13(): real
        reads this
    function reading14(): real
        reads this
    function reading10(): real
        reads this
    function reading1(): real
        reads this
    function reading2(): real
        reads this
    function reading3(): real
        reads this
    function reading8(): real
        reads this
    function reading9(): real
        reads this
    function reading4(): real
        reads this
    function reading15(): real
        reads this
    function reading5(): real
        reads this
    function reading6(): real
        reads this
    function reading7(): real
        reads this

    // Guard predicates (inlined from Java local variables and field assignments in step())
    //   below12 := reading12() <= 3.5
    //   below13 := reading13() <= 3.75
    //   below10 := reading10() <= 3.0
    //   below11 := reading11() <= 3.25
    //   below9 := reading9() <= 2.75
    //   below14 := reading14() <= 4.0
    //   below15 := reading15() <= 4.25
    //   below5 := reading5() <= 1.75
    //   below6 := reading6() <= 2.0
    //   below7 := reading7() <= 2.25
    //   below8 := reading8() <= 2.5
    //   below1 := reading1() <= 0.75
    //   below2 := reading2() <= 1.0
    //   below3 := reading3() <= 1.25
    //   below4 := reading4() <= 1.5

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
        ensures old(!(event == EndTask) && event == Advance && reading3() <= 1.25) ==> mode == M4
        ensures old(!(event == Advance && reading3() <= 1.25) && !(event == EndTask) && event == Retreat && !(reading3() <= 1.25)) ==> mode == M2
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading3() <= 1.25) {
            mode := M4;
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

    method transitionFromM4(event: InputEvent)
        requires mode == M4
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading4() <= 1.5) ==> mode == M5
        ensures old(!(event == Advance && reading4() <= 1.5) && !(event == EndTask) && event == Retreat && !(reading4() <= 1.5)) ==> mode == M3
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading4() <= 1.5) {
            mode := M5;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading4() <= 1.5)) {
            mode := M3;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M4;
        }
    }

    method transitionFromM5(event: InputEvent)
        requires mode == M5
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading5() <= 1.75) ==> mode == M6
        ensures old(!(event == Advance && reading5() <= 1.75) && !(event == EndTask) && event == Retreat && !(reading5() <= 1.75)) ==> mode == M4
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading5() <= 1.75) {
            mode := M6;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading5() <= 1.75)) {
            mode := M4;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M5;
        }
    }

    method transitionFromM6(event: InputEvent)
        requires mode == M6
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading6() <= 2.0) ==> mode == M7
        ensures old(!(event == Advance && reading6() <= 2.0) && !(event == EndTask) && event == Retreat && !(reading6() <= 2.0)) ==> mode == M5
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading6() <= 2.0) {
            mode := M7;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading6() <= 2.0)) {
            mode := M5;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M6;
        }
    }

    method transitionFromM7(event: InputEvent)
        requires mode == M7
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading7() <= 2.25) ==> mode == M8
        ensures old(!(event == Advance && reading7() <= 2.25) && !(event == EndTask) && event == Retreat && !(reading7() <= 2.25)) ==> mode == M6
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading7() <= 2.25) {
            mode := M8;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading7() <= 2.25)) {
            mode := M6;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M7;
        }
    }

    method transitionFromM8(event: InputEvent)
        requires mode == M8
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading8() <= 2.5) ==> mode == M9
        ensures old(!(event == Advance && reading8() <= 2.5) && !(event == EndTask) && event == Retreat && !(reading8() <= 2.5)) ==> mode == M7
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading8() <= 2.5) {
            mode := M9;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading8() <= 2.5)) {
            mode := M7;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M8;
        }
    }

    method transitionFromM9(event: InputEvent)
        requires mode == M9
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading9() <= 2.75) ==> mode == M10
        ensures old(!(event == Advance && reading9() <= 2.75) && !(event == EndTask) && event == Retreat && !(reading9() <= 2.75)) ==> mode == M8
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading9() <= 2.75) {
            mode := M10;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading9() <= 2.75)) {
            mode := M8;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M9;
        }
    }

    method transitionFromM10(event: InputEvent)
        requires mode == M10
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading10() <= 3.0) ==> mode == M11
        ensures old(!(event == Advance && reading10() <= 3.0) && !(event == EndTask) && event == Retreat && !(reading10() <= 3.0)) ==> mode == M9
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading10() <= 3.0) {
            mode := M11;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading10() <= 3.0)) {
            mode := M9;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M10;
        }
    }

    method transitionFromM11(event: InputEvent)
        requires mode == M11
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading11() <= 3.25) ==> mode == M12
        ensures old(!(event == Advance && reading11() <= 3.25) && !(event == EndTask) && event == Retreat && !(reading11() <= 3.25)) ==> mode == M10
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading11() <= 3.25) {
            mode := M12;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading11() <= 3.25)) {
            mode := M10;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M11;
        }
    }

    method transitionFromM12(event: InputEvent)
        requires mode == M12
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading12() <= 3.5) ==> mode == M13
        ensures old(!(event == Advance && reading12() <= 3.5) && !(event == EndTask) && event == Retreat && !(reading12() <= 3.5)) ==> mode == M11
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading12() <= 3.5) {
            mode := M13;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading12() <= 3.5)) {
            mode := M11;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M12;
        }
    }

    method transitionFromM13(event: InputEvent)
        requires mode == M13
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading13() <= 3.75) ==> mode == M14
        ensures old(!(event == Advance && reading13() <= 3.75) && !(event == EndTask) && event == Retreat && !(reading13() <= 3.75)) ==> mode == M12
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading13() <= 3.75) {
            mode := M14;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading13() <= 3.75)) {
            mode := M12;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M13;
        }
    }

    method transitionFromM14(event: InputEvent)
        requires mode == M14
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading14() <= 4.0) ==> mode == M15
        ensures old(!(event == Advance && reading14() <= 4.0) && !(event == EndTask) && event == Retreat && !(reading14() <= 4.0)) ==> mode == M13
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading14() <= 4.0) {
            mode := M15;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading14() <= 4.0)) {
            mode := M13;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M14;
        }
    }

    method transitionFromM15(event: InputEvent)
        requires mode == M15
        requires Valid()
        modifies this
        ensures old(!(event == EndTask) && event == Advance && reading15() <= 4.25) ==> mode == M1
        ensures old(!(event == Advance && reading15() <= 4.25) && !(event == EndTask) && event == Retreat && !(reading15() <= 4.25)) ==> mode == M14
        ensures Valid()
    {
        if (event == EndTask) {
            mode := Final;
            // action: Move(0.0)
        }
        else if (event == Advance && reading15() <= 4.25) {
            mode := M1;
            // action: Move(1.0)
        }
        else if (event == Retreat && !(reading15() <= 4.25)) {
            mode := M14;
            // action: Move(0.0)
        }
        else if (event == Tick) {
            mode := M15;
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
        else if (mode == M4) {
            transitionFromM4(event);
        }
        else if (mode == M5) {
            transitionFromM5(event);
        }
        else if (mode == M6) {
            transitionFromM6(event);
        }
        else if (mode == M7) {
            transitionFromM7(event);
        }
        else if (mode == M8) {
            transitionFromM8(event);
        }
        else if (mode == M9) {
            transitionFromM9(event);
        }
        else if (mode == M10) {
            transitionFromM10(event);
        }
        else if (mode == M11) {
            transitionFromM11(event);
        }
        else if (mode == M12) {
            transitionFromM12(event);
        }
        else if (mode == M13) {
            transitionFromM13(event);
        }
        else if (mode == M14) {
            transitionFromM14(event);
        }
        else if (mode == M15) {
            transitionFromM15(event);
        }
        else if (mode == Final) {
            transitionFromFinal(event);
        }
    }
}
