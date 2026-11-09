// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALISATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype PumpPosition = PUMP_OFF | PUMP_ON
datatype WaterFlow = NO_FLOW | FLOW
datatype OperatingMode = INITIALISATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype InputEvent = NoEvent | Transmission

// Static constants
const MAX_STEAM_W: real := 25.0
const PUMPS: int := 4
const FLOW_CHECK_DELAY: int := 2
const NORMAL_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 10.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const STEAM_RISE_U1: real := 2.0
const STEAM_FALL_U2: real := 2.0
const LIMIT_M2: real := 850.0
const LIMIT_M1: real := 150.0
const STOP_LIMIT: int := 3
const CYCLE: real := 5.0

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var programReadySent: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function unitsReady(): bool
        reads this
    function estimateUnreliable(): bool
        reads this
    function unitFailure(): bool
        reads this
    function startUpAbort(): bool
        reads this
    function startUpLevelReached(): bool
        reads this
    function emergencyCause(): bool
        reads this
    function levelFailure(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   levelDeviceRepaired := !emergencyCause() && !estimateUnreliable() && !levelFailure()
    //   unitsReady := unitsReady()
    //   estimateUnreliable := estimateUnreliable()
    //   unitFailure := unitFailure()
    //   startUpAbort := startUpAbort()
    //   operatingEmergency := emergencyCause() || (levelFailure() && estimateUnreliable())
    //   someUnitDefective := !emergencyCause() && !levelFailure() && unitFailure()
    //   startUpLevelReached := startUpLevelReached()
    //   announceReady := !startUpAbort() && startUpLevelReached() && !programReadySent
    //   allUnitsSound := !emergencyCause() && !levelFailure() && !unitFailure()
    //   rescueEmergency := emergencyCause() || estimateUnreliable()
    //   enterRescue := !emergencyCause() && levelFailure() && !estimateUnreliable()
    //   awaitingUnits := !startUpAbort() && programReadySent
    //   emergencyCause := emergencyCause()
    //   levelFailure := levelFailure()

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == INITIALISATION
        ensures Valid()
    {
        mode := INITIALISATION;
        programReadySent := false;
    }

    method transitionFromINITIALISATION(event: InputEvent)
        requires mode == INITIALISATION
        requires Valid()
        modifies this
        ensures old(event == Transmission && startUpAbort()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && startUpAbort()) && event == Transmission && !startUpAbort() && startUpLevelReached() && !programReadySent) ==> mode == INITIALISATION
        ensures old(!(event == Transmission && !startUpAbort() && startUpLevelReached() && !programReadySent) && !(event == Transmission && startUpAbort()) && event == Transmission && !startUpAbort() && programReadySent && !(unitsReady())) ==> mode == INITIALISATION
        ensures old(!(event == Transmission && !startUpAbort() && programReadySent && !(unitsReady())) && !(event == Transmission && !startUpAbort() && startUpLevelReached() && !programReadySent) && !(event == Transmission && startUpAbort()) && event == Transmission && !startUpAbort() && programReadySent && unitsReady() && !(unitFailure())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !startUpAbort() && programReadySent && unitsReady() && !(unitFailure())) && !(event == Transmission && !startUpAbort() && programReadySent && !(unitsReady())) && !(event == Transmission && !startUpAbort() && startUpLevelReached() && !programReadySent) && !(event == Transmission && startUpAbort()) && event == Transmission && !startUpAbort() && programReadySent && unitsReady() && unitFailure()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && startUpAbort()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !startUpAbort() && startUpLevelReached() && !programReadySent) {
            mode := INITIALISATION;
            // action: ProgramReady
        }
        else if (event == Transmission && !startUpAbort() && programReadySent && !(unitsReady())) {
            mode := INITIALISATION;
            // action: ProgramReady
        }
        else if (event == Transmission && !startUpAbort() && programReadySent && unitsReady() && !(unitFailure())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && !startUpAbort() && programReadySent && unitsReady() && unitFailure()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission) {
            mode := INITIALISATION;
            // action: Mode(INITIALISATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) && event == Transmission && !emergencyCause() && levelFailure() && !estimateUnreliable()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !emergencyCause() && levelFailure() && !estimateUnreliable()) && !(event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) && event == Transmission && !emergencyCause() && !levelFailure() && unitFailure()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !emergencyCause() && levelFailure() && !estimateUnreliable()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == Transmission && !emergencyCause() && !levelFailure() && unitFailure()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) && event == Transmission && !emergencyCause() && levelFailure() && !estimateUnreliable()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !emergencyCause() && levelFailure() && !estimateUnreliable()) && !(event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) && event == Transmission && !emergencyCause() && !levelFailure() && !unitFailure()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == Transmission && (emergencyCause() || (levelFailure() && estimateUnreliable()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !emergencyCause() && levelFailure() && !estimateUnreliable()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == Transmission && !emergencyCause() && !levelFailure() && !unitFailure()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == Transmission && (emergencyCause() || estimateUnreliable())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (emergencyCause() || estimateUnreliable())) && event == Transmission && !emergencyCause() && !estimateUnreliable() && !levelFailure()) ==> mode == NORMAL
        ensures old(!(event == Transmission && !emergencyCause() && !estimateUnreliable() && !levelFailure() && !(unitFailure())) && !(event == Transmission && (emergencyCause() || estimateUnreliable())) && event == Transmission && !emergencyCause() && !estimateUnreliable() && !levelFailure() && unitFailure()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && (emergencyCause() || estimateUnreliable())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !emergencyCause() && !estimateUnreliable() && !levelFailure() && !(unitFailure())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && !emergencyCause() && !estimateUnreliable() && !levelFailure() && unitFailure()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
    }

    method transitionFromEMERGENCY_STOP(event: InputEvent)
        requires mode == EMERGENCY_STOP
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == Transmission) {
            mode := EMERGENCY_STOP;
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == INITIALISATION) {
            transitionFromINITIALISATION(event);
        }
        else if (mode == NORMAL) {
            transitionFromNORMAL(event);
        }
        else if (mode == DEGRADED) {
            transitionFromDEGRADED(event);
        }
        else if (mode == RESCUE) {
            transitionFromRESCUE(event);
        }
        else if (mode == EMERGENCY_STOP) {
            transitionFromEMERGENCY_STOP(event);
        }
    }
}
