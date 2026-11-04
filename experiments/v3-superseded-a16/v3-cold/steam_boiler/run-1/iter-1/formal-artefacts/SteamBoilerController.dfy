// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | INITIALIZATION_FILLING | INITIALIZATION_DRAINING | INITIALIZATION_READY | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype OperatingMode = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype InputEvent = NoEvent | Transmission

// Static constants
const ROUNDING_ALLOWANCE: real := 0.001
const NORMAL_N1: real := 400.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 5.0
const STEAM_INCREASE_U1: real := 1.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const STEAM_DECREASE_U2: real := 1.0
const LIMIT_M2: real := 900.0
const STEAM_MAX_W: real := 10.0
const LIMIT_M1: real := 100.0
const CYCLE_DURATION: real := 5.0
const STOP_REPETITIONS: int := 3

class SteamBoilerController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function levelLowerBound(): real
        reads this
    function steamBoilerWaitingReceived(): bool
        reads this
    function successiveStops(): int
        reads this
    function steamDeviceFailed(): bool
        reads this
    function lowestLevelNextCycle(): real
        reads this
    function transmissionFailed(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelDeviceFailed(): bool
        reads this
    function anyPumpControllerFailed(): bool
        reads this
    function levelUpperBound(): real
        reads this
    function anyPumpFailed(): bool
        reads this
    function highestLevelNextCycle(): real
        reads this
    function steamRate(): real
        reads this
    function waterLevel(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   startupFault := (steamRate() != 0.0) || levelDeviceFailed()
    //   initProceeds := !(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())
    //   operatorStop := successiveStops() >= 3
    //   initUnitFault := (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed())
    //   estimateAboveN2 := levelLowerBound() + levelUpperBound() > 2.0 * 600.0
    //   unitsSound := (!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())
    //   transmissionFault := transmissionFailed()
    //   levelRiskBelowM1 := lowestLevelNextCycle() < 100.0
    //   initLevelHigh := (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0)
    //   rescueProceeds := !(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())
    //   initLevelNormal := (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)
    //   pumpControllerFaulty := anyPumpControllerFailed()
    //   levelDeviceFaulty := levelDeviceFailed()
    //   levelBelowN1 := waterLevel() < 400.0
    //   initEmergency := !(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived())
    //   levelAboveN2 := waterLevel() > 600.0
    //   operationFault := transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))
    //   levelRepairedSomeFaulty := (!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())
    //   levelRiskAboveM2 := highestLevelNextCycle() > 900.0
    //   readyProceeds := !(successiveStops() >= 3) && !transmissionFailed()
    //   levelDeviceLost := (!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed()
    //   estimateBelowN1 := levelLowerBound() + levelUpperBound() < 2.0 * 400.0
    //   plantWaiting := steamBoilerWaitingReceived()
    //   pumpFaulty := anyPumpFailed()
    //   unitsReadySomeFaulty := (!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())
    //   unitsReady := physicalUnitsReadyReceived()
    //   operationProceeds := !(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))
    //   levelStillFaulty := (!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()
    //   levelAtRisk := (lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)
    //   unitsReadyAllSound := (!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())
    //   otherUnitFaulty := steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()
    //   initLevelLow := (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0)
    //   readyUnitFault := (!(successiveStops() >= 3) && !transmissionFailed()) && ((steamRate() != 0.0) || levelDeviceFailed())
    //   rescueEmergency := !(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())
    //   steamDeviceFaulty := steamDeviceFailed()
    //   rescueFault := (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()
    //   readyEmergency := !(successiveStops() >= 3) && transmissionFailed()
    //   operationEmergency := !(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))
    //   initTransmissionFault := transmissionFailed() || physicalUnitsReadyReceived()
    //   steamNotZero := steamRate() != 0.0
    //   unitsFaulty := (!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())
    //   unitsNotYetReady := (!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && !physicalUnitsReadyReceived()
    //   levelRepairedAllSound := (!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == INITIALIZATION
        ensures Valid()
    {
        mode := INITIALIZATION;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !(steamBoilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !(steamBoilerWaitingReceived())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !(steamBoilerWaitingReceived())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) ==> mode == INITIALIZATION_DRAINING
        ensures old(!(event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) && !(event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !(steamBoilerWaitingReceived())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) ==> mode == INITIALIZATION_FILLING
        ensures old(!(event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) && !(event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) && !(event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !(steamBoilerWaitingReceived())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0))) ==> mode == INITIALIZATION_READY
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !(steamBoilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) {
            mode := INITIALIZATION_DRAINING;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) {
            mode := INITIALIZATION_FILLING;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && steamBoilerWaitingReceived() && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0))) {
            mode := INITIALIZATION_READY;
            // action: Mode(INITIALIZATION_MODE)
        }
    }

    method transitionFromINITIALIZATION_FILLING(event: InputEvent)
        requires mode == INITIALIZATION_FILLING
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) ==> mode == INITIALIZATION_DRAINING
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) ==> mode == INITIALIZATION_FILLING
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0))) ==> mode == INITIALIZATION_READY
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) {
            mode := INITIALIZATION_DRAINING;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) {
            mode := INITIALIZATION_FILLING;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0))) {
            mode := INITIALIZATION_READY;
            // action: Mode(INITIALIZATION_MODE)
        }
    }

    method transitionFromINITIALIZATION_DRAINING(event: InputEvent)
        requires mode == INITIALIZATION_DRAINING
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) ==> mode == INITIALIZATION_DRAINING
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) ==> mode == INITIALIZATION_FILLING
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0))) ==> mode == INITIALIZATION_READY
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || physicalUnitsReadyReceived()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && ((steamRate() != 0.0) || levelDeviceFailed()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && (waterLevel() > 600.0))) {
            mode := INITIALIZATION_DRAINING;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && (waterLevel() < 400.0))) {
            mode := INITIALIZATION_FILLING;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || physicalUnitsReadyReceived())) && !((steamRate() != 0.0) || levelDeviceFailed()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0))) {
            mode := INITIALIZATION_READY;
            // action: Mode(INITIALIZATION_MODE)
        }
    }

    method transitionFromINITIALIZATION_READY(event: InputEvent)
        requires mode == INITIALIZATION_READY
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && transmissionFailed()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && ((steamRate() != 0.0) || levelDeviceFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && !(successiveStops() >= 3) && transmissionFailed()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && !(successiveStops() >= 3) && transmissionFailed()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && ((steamRate() != 0.0) || levelDeviceFailed()))) && !(event == Transmission && !(successiveStops() >= 3) && transmissionFailed()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && !physicalUnitsReadyReceived())) ==> mode == INITIALIZATION_READY
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && ((steamRate() != 0.0) || levelDeviceFailed()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !transmissionFailed()) && !((steamRate() != 0.0) || levelDeviceFailed()) && !physicalUnitsReadyReceived())) {
            mode := INITIALIZATION_READY;
            // action: Mode(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) ==> mode == RESCUE
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && waterLevel() > 600.0) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && waterLevel() > 600.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && waterLevel() > 600.0) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) ==> mode == RESCUE
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && waterLevel() > 600.0) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && waterLevel() > 600.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) && !(event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && (transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && levelDeviceFailed())) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && waterLevel() < 400.0) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && waterLevel() > 600.0) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0)))) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && levelLowerBound() + levelUpperBound() < 2.0 * 400.0) ==> mode == RESCUE
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && !(levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && levelLowerBound() + levelUpperBound() > 2.0 * 600.0) ==> mode == RESCUE
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && !(levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && levelLowerBound() + levelUpperBound() > 2.0 * 600.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && !(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && !(levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && !(levelLowerBound() + levelUpperBound() > 2.0 * 600.0)) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && ((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && !(steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && !levelDeviceFailed() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && levelLowerBound() + levelUpperBound() < 2.0 * 400.0) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && !(levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && levelLowerBound() + levelUpperBound() > 2.0 * 600.0) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !((transmissionFailed() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || ((lowestLevelNextCycle() < 100.0) || (highestLevelNextCycle() > 900.0))) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed()) && !(levelLowerBound() + levelUpperBound() < 2.0 * 400.0) && !(levelLowerBound() + levelUpperBound() > 2.0 * 600.0)) {
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
            // action: Mode(EMERGENCY_STOP_MODE)
        }
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == INITIALIZATION) {
            transitionFromINITIALIZATION(event);
        }
        else if (mode == INITIALIZATION_FILLING) {
            transitionFromINITIALIZATION_FILLING(event);
        }
        else if (mode == INITIALIZATION_DRAINING) {
            transitionFromINITIALIZATION_DRAINING(event);
        }
        else if (mode == INITIALIZATION_READY) {
            transitionFromINITIALIZATION_READY(event);
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
