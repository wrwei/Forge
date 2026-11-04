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
    var readyAnnounced: bool
    var evacuationValve: ValvePosition

    // Abstracted functions (from dependency objects -- uninterpreted)
    function stopOrTransmissionFailure(): bool
        reads this
    function steamBoilerWaitingReceived(): bool
        reads this
    function estimateUnreliable(): bool
        reads this
    function operatingEmergency(): bool
        reads this
    function startupCheckFailed(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function predictedLevelBelowNormal(): bool
        reads this
    function predictedLevelAboveNormal(): bool
        reads this
    function levelDeviceDefective(): bool
        reads this
    function otherUnitDefective(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   operatingAbort := operatingEmergency() || (levelDeviceDefective() && estimateUnreliable())
    //   startupFault := startupCheckFailed()
    //   levelLow := levelBelowNormal()
    //   unitsReady := physicalUnitsReadyReceived()
    //   estimateUnreliable := estimateUnreliable()
    //   levelHigh := levelAboveNormal()
    //   operatingEmergency := operatingEmergency()
    //   predictedLow := predictedLevelBelowNormal()
    //   rescueAbort := operatingEmergency() || estimateUnreliable()
    //   otherUnitDefective := otherUnitDefective()
    //   allUnitsSound := !levelDeviceDefective() && !otherUnitDefective()
    //   predictedHigh := predictedLevelAboveNormal()
    //   valveOpen := evacuationValve == OPEN
    //   initAbort := stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed())
    //   stopOrTransmissionFault := stopOrTransmissionFailure()
    //   plantWaiting := steamBoilerWaitingReceived()
    //   levelDefective := levelDeviceDefective()

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
        evacuationValve := CLOSED;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && physicalUnitsReadyReceived()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && readyAnnounced) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && steamBoilerWaitingReceived() && levelAboveNormal() && !(evacuationValve == OPEN)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && steamBoilerWaitingReceived() && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal() && evacuationValve == OPEN) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && steamBoilerWaitingReceived() && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal()) && !(event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && steamBoilerWaitingReceived() && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && steamBoilerWaitingReceived() && !(levelAboveNormal()) && evacuationValve == OPEN) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && steamBoilerWaitingReceived() && !(levelAboveNormal()) && evacuationValve == OPEN) && !(event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal()) && !(event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal() && evacuationValve == OPEN) && !(event == TransmissionReceived && steamBoilerWaitingReceived() && levelAboveNormal() && !(evacuationValve == OPEN)) && !(event == TransmissionReceived && readyAnnounced) && !(event == TransmissionReceived && physicalUnitsReadyReceived()) && !(event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) && !(event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) && event == TransmissionReceived && steamBoilerWaitingReceived() && !(levelAboveNormal())) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && (stopOrTransmissionFailure() || (physicalUnitsReadyReceived() && !readyAnnounced) || (steamBoilerWaitingReceived() && startupCheckFailed()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && physicalUnitsReadyReceived() && otherUnitDefective()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && physicalUnitsReadyReceived()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && readyAnnounced) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && steamBoilerWaitingReceived() && levelAboveNormal() && !(evacuationValve == OPEN)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal() && evacuationValve == OPEN) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && steamBoilerWaitingReceived() && levelBelowNormal()) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && steamBoilerWaitingReceived() && !(levelAboveNormal()) && evacuationValve == OPEN) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && steamBoilerWaitingReceived() && !(levelAboveNormal())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && levelDeviceDefective()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && levelDeviceDefective()) && !(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && otherUnitDefective()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && otherUnitDefective()) && !(event == TransmissionReceived && levelDeviceDefective()) && !(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && predictedLevelAboveNormal()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && predictedLevelAboveNormal()) && !(event == TransmissionReceived && otherUnitDefective()) && !(event == TransmissionReceived && levelDeviceDefective()) && !(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && predictedLevelBelowNormal()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && levelDeviceDefective()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && otherUnitDefective()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && predictedLevelAboveNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && predictedLevelBelowNormal()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && levelDeviceDefective()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && levelDeviceDefective()) && !(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && !(otherUnitDefective())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(otherUnitDefective())) && !(event == TransmissionReceived && levelDeviceDefective()) && !(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && predictedLevelAboveNormal()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && predictedLevelAboveNormal()) && !(event == TransmissionReceived && !(otherUnitDefective())) && !(event == TransmissionReceived && levelDeviceDefective()) && !(event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) && event == TransmissionReceived && predictedLevelBelowNormal()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && (operatingEmergency() || (levelDeviceDefective() && estimateUnreliable()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && levelDeviceDefective()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && !(otherUnitDefective())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && predictedLevelAboveNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && predictedLevelBelowNormal()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (operatingEmergency() || estimateUnreliable())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (operatingEmergency() || estimateUnreliable())) && event == TransmissionReceived && !levelDeviceDefective() && !otherUnitDefective()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !levelDeviceDefective() && !otherUnitDefective()) && !(event == TransmissionReceived && (operatingEmergency() || estimateUnreliable())) && event == TransmissionReceived && !(levelDeviceDefective())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(levelDeviceDefective())) && !(event == TransmissionReceived && !levelDeviceDefective() && !otherUnitDefective()) && !(event == TransmissionReceived && (operatingEmergency() || estimateUnreliable())) && event == TransmissionReceived && predictedLevelAboveNormal()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && predictedLevelAboveNormal()) && !(event == TransmissionReceived && !(levelDeviceDefective())) && !(event == TransmissionReceived && !levelDeviceDefective() && !otherUnitDefective()) && !(event == TransmissionReceived && (operatingEmergency() || estimateUnreliable())) && event == TransmissionReceived && predictedLevelBelowNormal()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && (operatingEmergency() || estimateUnreliable())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && !levelDeviceDefective() && !otherUnitDefective()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && !(levelDeviceDefective())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && predictedLevelAboveNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && predictedLevelBelowNormal()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived) {
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
