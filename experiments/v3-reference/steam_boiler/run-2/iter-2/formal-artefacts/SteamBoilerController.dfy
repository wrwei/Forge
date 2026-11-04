// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ModeValue = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype WaterFlow = FLOWING | NOT_FLOWING
datatype InputEvent = NoEvent | Transmission

// Static constants
const MAXIMAL_LIMIT_M2: real := 850.0
const MINIMAL_LIMIT_M1: real := 150.0
const MAXIMAL_STEAM_W: real := 25.0
const CYCLE_SECONDS: real := 5.0
const STOPS_FOR_EMERGENCY: int := 3
const PUMP_CAPACITY_P: real := 10.0
const STEAM_INCREASE_U1: real := 4.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const SETTLED: int := 3
const T: real := 5.0
const STEAM_DECREASE_U2: real := 4.0
const MINIMAL_NORMAL_N1: real := 400.0
const MAXIMAL_NORMAL_N2: real := 600.0

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var steamBoilerWaitingSeen: bool
    var readyAnnounced: bool
    var valveOpen: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function steamBoilerWaitingReceived(): bool
        reads this
    function levelRiskOfLimit(): bool
        reads this
    function levelMayFallBelowNormal(): bool
        reads this
    function levelMayRiseAboveNormal(): bool
        reads this
    function operatorStopConfirmed(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function otherUnitOutOfOrder(): bool
        reads this
    function pumpControllerOutOfOrder(): bool
        reads this
    function levelDeviceOutOfOrder(): bool
        reads this
    function steamDeviceOutOfOrder(): bool
        reads this
    function transmissionFailure(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   steamDeviceFailed := steamDeviceOutOfOrder()
    //   stopDemanded := operatorStopConfirmed()
    //   transmissionFailed := transmissionFailure()
    //   levelDeviceFailed := levelDeviceOutOfOrder()
    //   valveIsOpen := valveOpen
    //   pumpControllerFailed := pumpControllerOutOfOrder()
    //   otherUnitFailed := otherUnitOutOfOrder()
    //   levelBelowN1 := levelBelowNormal()
    //   plantWaitingNow := steamBoilerWaitingReceived()
    //   levelMayRiseAboveN2 := levelMayRiseAboveNormal()
    //   levelRisk := levelRiskOfLimit()
    //   levelAboveN2 := levelAboveNormal()
    //   levelMayFallBelowN1 := levelMayFallBelowNormal()
    //   programReady := readyAnnounced
    //   plantWaiting := steamBoilerWaitingSeen
    //   unitsReadyNow := physicalUnitsReadyReceived()

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
        steamBoilerWaitingSeen := false;
        readyAnnounced := false;
        valveOpen := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == Transmission && operatorStopConfirmed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal() && !(valveOpen)) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == Transmission && operatorStopConfirmed()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && !(steamBoilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(steamBoilerWaitingSeen) && steamBoilerWaitingReceived()) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == Transmission && operatorStopConfirmed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == Transmission && operatorStopConfirmed()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == Transmission && operatorStopConfirmed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && operatorStopConfirmed()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && levelDeviceOutOfOrder()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == Transmission && operatorStopConfirmed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) && !(event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) && !(event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) && !(event == Transmission && operatorStopConfirmed()) && event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == Transmission && operatorStopConfirmed()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && transmissionFailure()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && levelRiskOfLimit()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && steamDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == Transmission && !(operatorStopConfirmed()) && !(transmissionFailure()) && !(levelRiskOfLimit()) && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
    }

    method transitionFromEMERGENCY_STOP(event: InputEvent)
        requires mode == EMERGENCY_STOP
        requires Valid()
        modifies this
        ensures Valid()
    {
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
