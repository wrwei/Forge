// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype PumpPosition = PUMP_OFF | PUMP_ON
datatype UnitCondition = SOUND | FAILED_UNACKNOWLEDGED | FAILED_ACKNOWLEDGED
datatype ValvePosition = VALVE_CLOSED | VALVE_OPEN
datatype WaterFlow = NOT_FLOWING | FLOWING
datatype OperatingMode = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const CAPACITY: real := 1000.0
const STEAM_UNIT: int := LEVEL_UNIT + 1
const PUMPS: int := 4
const CYCLE_SECONDS: real := 5.0
const MAX_NORMAL: real := 600.0
const SETTLED_AFTER: int := 3
const MIN_LIMIT: real := 150.0
const MAX_STEAM_INCREASE: real := 2.0
const PUMP_COUNT: int := 4
const MAX_LIMIT: real := 850.0
const MAX_STEAM_RATE: real := 25.0
const MIN_NORMAL: real := 400.0
const MAX_STEAM_DECREASE: real := 2.0
const PUMP_CAPACITY: real := 10.0
const LEVEL_UNIT: int := 2 * 4
const UNIT_COUNT: int := STEAM_UNIT + 1
const STOP_REPEAT_LIMIT: int := 3
const CYCLE: real := 5.0

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var readyAnnounced: bool
    var valveOpen: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function steamBoilerWaitingReceived(): bool
        reads this
    function levelLimitRisk(): bool
        reads this
    function nonLevelUnitOutOfOrder(): bool
        reads this
    function steamOrPumpControllerOutOfOrder(): bool
        reads this
    function steamRateNonZero(): bool
        reads this
    function levelDeviceOutOfOrder(): bool
        reads this
    function stopDemanded(): bool
        reads this
    function transmissionFailed(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   belowNormal := levelBelowNormal()
    //   operatorStop := stopDemanded()
    //   unitsReady := physicalUnitsReadyReceived()
    //   transmissionLost := transmissionFailed()
    //   estimationUnitFailed := steamOrPumpControllerOutOfOrder()
    //   otherUnitFailed := nonLevelUnitOutOfOrder()
    //   levelFailed := levelDeviceOutOfOrder()
    //   levelRisk := levelLimitRisk()
    //   regulationClear := !stopDemanded() && !transmissionFailed() && !levelLimitRisk()
    //   messagesClear := !stopDemanded() && !transmissionFailed()
    //   aboveNormal := levelAboveNormal()
    //   withinNormal := !levelBelowNormal() && !levelAboveNormal()
    //   plantWaiting := steamBoilerWaitingReceived()
    //   steamNonZero := steamRateNonZero()
    //   startupHealthy := !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder()

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
        readyAnnounced := false;
        valveOpen := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && !(valveOpen) && levelBelowNormal()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && !(valveOpen) && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && !(valveOpen) && !(levelBelowNormal())) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && stopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && steamRateNonZero()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !(steamBoilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && !(nonLevelUnitOutOfOrder())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && readyAnnounced) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && !(valveOpen) && levelBelowNormal()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && steamBoilerWaitingReceived() && !steamRateNonZero() && !levelDeviceOutOfOrder() && !(physicalUnitsReadyReceived()) && !(readyAnnounced) && !(levelAboveNormal()) && !(valveOpen) && !(levelBelowNormal())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && levelBelowNormal()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && !(levelBelowNormal()) && levelAboveNormal()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && !(levelBelowNormal()) && levelAboveNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && !levelBelowNormal() && !levelAboveNormal()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && stopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && levelBelowNormal()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && !(levelBelowNormal()) && levelAboveNormal()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder()) && !levelBelowNormal() && !levelAboveNormal()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && levelBelowNormal()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && !(levelBelowNormal()) && levelAboveNormal()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && !(levelBelowNormal()) && levelAboveNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && !levelBelowNormal() && !levelAboveNormal()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && stopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && steamOrPumpControllerOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && levelDeviceOutOfOrder() && !(steamOrPumpControllerOutOfOrder())) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && levelBelowNormal()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && !(levelBelowNormal()) && levelAboveNormal()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder() && !levelBelowNormal() && !levelAboveNormal()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelBelowNormal()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelBelowNormal()) && levelAboveNormal()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelBelowNormal()) && levelAboveNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelBelowNormal()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) && !(event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) && !(event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) && !(event == TransmissionReceived && stopDemanded()) && event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !levelBelowNormal() && !levelAboveNormal()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && stopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopDemanded()) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && levelLimitRisk()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && steamOrPumpControllerOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && nonLevelUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelBelowNormal()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelBelowNormal()) && levelAboveNormal()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !stopDemanded() && !transmissionFailed() && !levelLimitRisk() && !(steamOrPumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !levelBelowNormal() && !levelAboveNormal()) {
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
        if (event == TransmissionReceived) {
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
