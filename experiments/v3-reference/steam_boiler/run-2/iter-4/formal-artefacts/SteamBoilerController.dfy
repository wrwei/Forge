// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ModeValue = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype WaterFlow = FLOWING | NOT_FLOWING
datatype InputEvent = NoEvent | StopConfirmed | TransmissionFault | SteamBoilerWaiting | PhysicalUnitsReady | Transmission | LevelRisk

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
    var pumpsOpen: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function otherUnitOutOfOrder(): bool
        reads this
    function levelMayFallBelowNormal(): bool
        reads this
    function pumpControllerOutOfOrder(): bool
        reads this
    function levelDeviceOutOfOrder(): bool
        reads this
    function steamDeviceOutOfOrder(): bool
        reads this
    function levelMayRiseAboveNormal(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   levelBelowN1 := levelBelowNormal()
    //   levelMayRiseAboveN2 := levelMayRiseAboveNormal()
    //   steamDeviceFailed := steamDeviceOutOfOrder()
    //   levelAboveN2 := levelAboveNormal()
    //   levelMayFallBelowN1 := levelMayFallBelowNormal()
    //   programReady := readyAnnounced
    //   plantWaiting := steamBoilerWaitingSeen
    //   levelDeviceFailed := levelDeviceOutOfOrder()
    //   valveIsOpen := valveOpen
    //   pumpControllerFailed := pumpControllerOutOfOrder()
    //   otherUnitFailed := otherUnitOutOfOrder()

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
        pumpsOpen := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(!(event == TransmissionFault) && !(event == StopConfirmed) && event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) ==> mode == INITIALIZATION
        ensures old(!(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamBoilerWaitingSeen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) && !(event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) && !(event == Transmission && !(steamBoilerWaitingSeen)) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) && !(event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == StopConfirmed) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionFault) {
            mode := EMERGENCY_STOP;
        }
        else if (event == SteamBoilerWaiting && !(steamBoilerWaitingSeen)) {
            mode := INITIALIZATION;
        }
        else if (event == PhysicalUnitsReady && readyAnnounced && levelDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == PhysicalUnitsReady && readyAnnounced && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && !(steamBoilerWaitingSeen)) {
            mode := INITIALIZATION;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && levelDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && steamDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && readyAnnounced) {
            mode := INITIALIZATION;
            // action: ProgramReady
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: Valve
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal() && valveOpen) {
            mode := INITIALIZATION;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && valveOpen) {
            mode := INITIALIZATION;
            // action: Valve
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal() && !(valveOpen)) {
            mode := INITIALIZATION;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(levelDeviceOutOfOrder()) && !(steamDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ProgramReady
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(!(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == StopConfirmed) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionFault) {
            mode := EMERGENCY_STOP;
        }
        else if (event == LevelRisk) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && levelDeviceOutOfOrder()) {
            mode := RESCUE;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelMayFallBelowNormal()) {
            mode := NORMAL;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) {
            mode := NORMAL;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) {
            mode := NORMAL;
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(!(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == StopConfirmed) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionFault) {
            mode := EMERGENCY_STOP;
        }
        else if (event == LevelRisk) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && levelDeviceOutOfOrder()) {
            mode := RESCUE;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelMayFallBelowNormal()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) {
            mode := DEGRADED;
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(!(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && steamDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && steamDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && steamDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && steamDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && steamDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) ==> mode == RESCUE
        ensures old(!(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) && !(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) && !(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) && !(event == Transmission && steamDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == StopConfirmed) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionFault) {
            mode := EMERGENCY_STOP;
        }
        else if (event == LevelRisk) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && steamDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && !(steamDeviceOutOfOrder()) && pumpControllerOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && levelMayFallBelowNormal()) {
            mode := RESCUE;
        }
        else if (event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && levelMayRiseAboveNormal()) {
            mode := RESCUE;
        }
        else if (event == Transmission && !(steamDeviceOutOfOrder()) && !(pumpControllerOutOfOrder()) && levelDeviceOutOfOrder() && !(levelMayFallBelowNormal()) && !(levelMayRiseAboveNormal())) {
            mode := RESCUE;
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
