// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ModeValue = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype WaterFlow = FLOWING | NOT_FLOWING
datatype InputEvent = NoEvent | StopConfirmed | TransmissionFault | StartUpFailure | SteamBoilerWaiting | PhysicalUnitsReady | Transmission | LevelRisk | EstimateUnreliable

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
    function levelDeviceOutOfOrder(): bool
        reads this
    function levelTooLow(): bool
        reads this
    function levelTooHigh(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   programReady := readyAnnounced
    //   levelTooLow := levelTooLow()
    //   levelTooHigh := levelTooHigh()
    //   plantWaiting := steamBoilerWaitingSeen
    //   levelDeviceFailed := levelDeviceOutOfOrder()
    //   valveIsOpen := valveOpen
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
        ensures old(!(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) && !(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) && !(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && readyAnnounced) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && readyAnnounced) && !(event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) && !(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && levelTooHigh() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && levelTooHigh() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && readyAnnounced) && !(event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) && !(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && levelTooHigh() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && readyAnnounced) && !(event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) && !(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && levelTooLow() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && levelTooLow() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && valveOpen) && !(event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && levelTooHigh() && !(valveOpen)) && !(event == Transmission && steamBoilerWaitingSeen && readyAnnounced) && !(event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) && !(event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) && !(event == SteamBoilerWaiting) && !(event == StartUpFailure) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && !(levelTooLow()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == StopConfirmed) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionFault) {
            mode := EMERGENCY_STOP;
        }
        else if (event == StartUpFailure) {
            mode := EMERGENCY_STOP;
        }
        else if (event == SteamBoilerWaiting) {
            mode := INITIALIZATION;
        }
        else if (event == PhysicalUnitsReady && readyAnnounced && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == PhysicalUnitsReady && readyAnnounced && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && readyAnnounced) {
            mode := INITIALIZATION;
            // action: ProgramReady
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && levelTooHigh() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: Valve
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && valveOpen) {
            mode := INITIALIZATION;
            // action: Valve
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && levelTooLow() && !(valveOpen)) {
            mode := INITIALIZATION;
        }
        else if (event == Transmission && steamBoilerWaitingSeen && !(readyAnnounced) && !(levelTooHigh()) && !(levelTooLow()) && !(valveOpen)) {
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
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelTooLow()) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelTooLow()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelTooLow()) && levelTooHigh()) ==> mode == NORMAL
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
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelTooLow()) {
            mode := NORMAL;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelTooLow()) && levelTooHigh()) {
            mode := NORMAL;
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(!(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && levelDeviceOutOfOrder()) ==> mode == RESCUE
        ensures old(!(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelTooLow()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelTooLow()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == Transmission && levelDeviceOutOfOrder()) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelTooLow()) && levelTooHigh()) ==> mode == DEGRADED
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
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelTooLow()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelTooLow()) && levelTooHigh()) {
            mode := DEGRADED;
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(!(event == EstimateUnreliable) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == EstimateUnreliable) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == EstimateUnreliable) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && levelDeviceOutOfOrder() && levelTooLow()) ==> mode == RESCUE
        ensures old(!(event == Transmission && levelDeviceOutOfOrder() && levelTooLow()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == EstimateUnreliable) && !(event == LevelRisk) && !(event == TransmissionFault) && !(event == StopConfirmed) && event == Transmission && levelDeviceOutOfOrder() && !(levelTooLow()) && levelTooHigh()) ==> mode == RESCUE
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
        else if (event == EstimateUnreliable) {
            mode := EMERGENCY_STOP;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == Transmission && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == Transmission && levelDeviceOutOfOrder() && levelTooLow()) {
            mode := RESCUE;
        }
        else if (event == Transmission && levelDeviceOutOfOrder() && !(levelTooLow()) && levelTooHigh()) {
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
