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
    function nonLevelUnitOutOfOrder(): bool
        reads this
    function initializationAbortDemanded(): bool
        reads this
    function levelDeviceOutOfOrder(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function emergencyStopDemanded(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   belowNormal := levelBelowNormal()
    //   emergencyStop := emergencyStopDemanded()
    //   levelFailed := levelDeviceOutOfOrder()
    //   unitsReady := physicalUnitsReadyReceived()
    //   aboveNormal := levelAboveNormal()
    //   initializationAbort := initializationAbortDemanded()
    //   plantWaiting := steamBoilerWaitingReceived()
    //   otherUnitFailed := nonLevelUnitOutOfOrder()

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
        ensures old(event == TransmissionReceived && initializationAbortDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && !(steamBoilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && physicalUnitsReadyReceived()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && readyAnnounced) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && levelAboveNormal() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && levelAboveNormal()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && levelAboveNormal()) && !(event == TransmissionReceived && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && valveOpen) && !(event == TransmissionReceived && levelAboveNormal()) && !(event == TransmissionReceived && levelAboveNormal() && !(valveOpen)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initializationAbortDemanded()) && event == TransmissionReceived && levelBelowNormal()) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && initializationAbortDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(steamBoilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && physicalUnitsReadyReceived() && nonLevelUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && physicalUnitsReadyReceived()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && readyAnnounced) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && levelAboveNormal() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && levelAboveNormal()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && valveOpen) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && levelBelowNormal()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && emergencyStopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && nonLevelUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelBelowNormal()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && levelBelowNormal()) && !(event == TransmissionReceived && nonLevelUnitOutOfOrder()) && !(event == TransmissionReceived && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelAboveNormal()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && emergencyStopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && levelDeviceOutOfOrder()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && nonLevelUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && levelBelowNormal()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && levelAboveNormal()) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && emergencyStopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && !(nonLevelUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelBelowNormal()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && levelBelowNormal()) && !(event == TransmissionReceived && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelAboveNormal()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && emergencyStopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && levelDeviceOutOfOrder()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(nonLevelUnitOutOfOrder())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && levelBelowNormal()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && levelAboveNormal()) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && emergencyStopDemanded()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && !(levelDeviceOutOfOrder())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(levelDeviceOutOfOrder())) && !(event == TransmissionReceived && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelBelowNormal()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && levelBelowNormal()) && !(event == TransmissionReceived && !(levelDeviceOutOfOrder())) && !(event == TransmissionReceived && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) && !(event == TransmissionReceived && emergencyStopDemanded()) && event == TransmissionReceived && levelAboveNormal()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && emergencyStopDemanded()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(levelDeviceOutOfOrder()) && !(nonLevelUnitOutOfOrder())) {
            mode := NORMAL;
            // action: Mode(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(levelDeviceOutOfOrder())) {
            mode := DEGRADED;
            // action: Mode(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && levelBelowNormal()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && levelAboveNormal()) {
            mode := RESCUE;
            // action: Mode(RESCUE_MODE)
        }
        else if (event == TransmissionReceived) {
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
