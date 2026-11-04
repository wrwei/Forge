// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype PumpPosition = OFF | ON
datatype ValvePosition = CLOSED | OPEN
datatype WaterFlow = NO_FLOW | FLOWING
datatype PumpOrder = NO_ORDER | OPEN_ORDER | CLOSE_ORDER
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const LIMIT_MAX_M2: real := 850.0
const LIMIT_MIN_M1: real := 150.0
const NORMAL_MIN_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const STEAM_DECREASE_MAX_U2: real := 4.0
const PUMP_START_LATENCY_SECONDS: real := 5.0
const PUMP_CAPACITY_P: real := 10.0
const STEAM_INCREASE_MAX_U1: real := 4.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const NORMAL_MAX_N2: real := 600.0
const STEAM_MAX_W: real := 25.0
const STOP_REPETITIONS: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var boilerWaiting: bool
    var readyAnnounced: bool
    var evacuationValve: ValvePosition

    // Abstracted functions (from dependency objects -- uninterpreted)
    function steamBoilerWaitingReceived(): bool
        reads this
    function successiveStops(): int
        reads this
    function levelLimitRisk(): bool
        reads this
    function steamDeviceDefective(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function steamRateZero(): bool
        reads this
    function predictedLevelAboveNormal(): bool
        reads this
    function levelDeviceDefective(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function pumpDefective(): bool
        reads this
    function transmissionFailure(): bool
        reads this
    function pumpControllerDefective(): bool
        reads this
    function predictedLevelBelowNormal(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   levelLow := levelBelowNormal()
    //   estimateLost := !((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective())
    //   otherUnitsOnlyDefective := !((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())
    //   estimateUnreliable := steamDeviceDefective() || pumpControllerDefective()
    //   levelHigh := levelAboveNormal()
    //   waitingMessage := steamBoilerWaitingReceived()
    //   initSteamNotZero := !((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero()
    //   transmissionFault := transmissionFailure()
    //   otherUnitDefective := steamDeviceDefective() || pumpDefective() || pumpControllerDefective()
    //   levelRaise := levelBelowNormal() && !levelAboveNormal()
    //   steamDefective := steamDeviceDefective()
    //   initRunning := !((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()
    //   steamZero := steamRateZero()
    //   plantWaiting := boilerWaiting || steamBoilerWaitingReceived()
    //   limitRisk := levelLimitRisk()
    //   unitsReadyMessage := physicalUnitsReadyReceived()
    //   pumpsOn := predictedLevelBelowNormal() && !predictedLevelAboveNormal()
    //   operatingAbort := (successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()
    //   initLevelFailure := !((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective()
    //   predictedLow := predictedLevelBelowNormal()
    //   levelAdjust := (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced
    //   pumpControlDefective := pumpControllerDefective()
    //   levelLostWithEstimate := !((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective())
    //   levelInBand := !levelBelowNormal() && !levelAboveNormal()
    //   readyRepeat := (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced
    //   stopDemanded := successiveStops() >= 3
    //   unitsReadyAllSound := (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())
    //   allUnitsSound := !((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())
    //   pumpsKept := !predictedLevelBelowNormal() && !predictedLevelAboveNormal()
    //   rescueContinues := !((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()
    //   predictedHigh := predictedLevelAboveNormal()
    //   valveOpen := evacuationValve == OPEN
    //   levelBackSomeDefective := (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(steamDeviceDefective() || pumpControllerDefective())
    //   initAbort := (successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)
    //   unitsReadySomeDefective := (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())
    //   levelLostNoEstimate := !((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())
    //   pumpDefective := pumpDefective()
    //   pumpsOff := predictedLevelAboveNormal()
    //   levelDefective := levelDeviceDefective()
    //   initIdle := !((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived())

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
        boilerWaiting := false;
        readyAnnounced := false;
        evacuationValve := CLOSED;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && evacuationValve == OPEN) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && !(evacuationValve == OPEN)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && !levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && !levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && !levelBelowNormal() && !levelAboveNormal() && !(evacuationValve == OPEN)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && !steamRateZero())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && levelDeviceDefective())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && !(boilerWaiting || steamBoilerWaitingReceived()))) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && physicalUnitsReadyReceived() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && readyAnnounced)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && !(evacuationValve == OPEN)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelAboveNormal() && evacuationValve == OPEN) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && levelBelowNormal() && !levelAboveNormal() && !(evacuationValve == OPEN)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && !levelBelowNormal() && !levelAboveNormal() && evacuationValve == OPEN) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced)) && (boilerWaiting || steamBoilerWaitingReceived()) && steamRateZero() && !levelDeviceDefective()) && !physicalUnitsReadyReceived() && !readyAnnounced) && !levelBelowNormal() && !levelAboveNormal() && !(evacuationValve == OPEN)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !predictedLevelBelowNormal() && !predictedLevelAboveNormal()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !predictedLevelBelowNormal() && !predictedLevelAboveNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !predictedLevelBelowNormal() && !predictedLevelAboveNormal()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && levelDeviceDefective() && !(steamDeviceDefective() || pumpControllerDefective()))) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelAboveNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !predictedLevelBelowNormal() && !predictedLevelAboveNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(steamDeviceDefective() || pumpControllerDefective()))) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelAboveNormal()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelAboveNormal()) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelAboveNormal()) && !(event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && !predictedLevelBelowNormal() && !predictedLevelAboveNormal()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && (steamDeviceDefective() || pumpControllerDefective()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && ((!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !levelDeviceDefective() && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(steamDeviceDefective() || pumpControllerDefective()))) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelAboveNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && predictedLevelBelowNormal() && !predictedLevelAboveNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelLimitRisk()) && !(steamDeviceDefective() || pumpControllerDefective()) && levelDeviceDefective()) && !predictedLevelBelowNormal() && !predictedLevelAboveNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
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
