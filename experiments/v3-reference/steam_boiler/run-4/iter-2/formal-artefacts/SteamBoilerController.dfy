// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype FlowObservation = WATER_FLOWS | NO_WATER_FLOW
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const CAPACITY: real := 1000.0
const DETECTED: real := 0.0  /* SUBSTITUTED: unhandled multi-arg constructor call */
const PUMPS: int := 4
const TOLERANCE: real := 0.000000001
const MAX_NORMAL: real := 600.0
const MAX_STEAM: real := 10.0
const MIN_LIMIT: real := 100.0
const DT: real := 5.0
const PUMP_COUNT: int := 4
const MAX_LIMIT: real := 900.0
const STEAM_DECREASE_GRADIENT: real := 1.0
const MIN_NORMAL: real := 400.0
const SOUND: real := 0.0  /* SUBSTITUTED: unhandled multi-arg constructor call */
const PUMP_CAPACITY: real := 5.0
const CYCLE_DURATION: real := 5.0
const STEAM_INCREASE_GRADIENT: real := 1.0
const STOP_LIMIT: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var valveOpen: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function rescueEmergency(): bool
        reads this
    function levelUnderNormalBand(): bool
        reads this
    function steamBoilerWaitingReceived(): bool
        reads this
    function initialisationEmergency(): bool
        reads this
    function operationEmergency(): bool
        reads this
    function levelUnitFailed(): bool
        reads this
    function nonLevelUnitFailed(): bool
        reads this
    function levelOverNormalBand(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   operatingAbort := operationEmergency()
    //   unitsReady := physicalUnitsReadyReceived()
    //   levelFailed := levelUnitFailed()
    //   evacuationOpen := valveOpen
    //   initAbort := initialisationEmergency()
    //   rescueAbort := rescueEmergency()
    //   plantWaiting := steamBoilerWaitingReceived()
    //   levelBelowNormal := levelUnderNormalBand()
    //   otherUnitFailed := nonLevelUnitFailed()
    //   levelAboveNormal := levelOverNormalBand()

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
        valveOpen := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && initialisationEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && !(valveOpen)) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && valveOpen) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && !(valveOpen)) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && !(levelOverNormalBand()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && !(levelOverNormalBand()) && valveOpen) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && valveOpen) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && !(valveOpen)) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelUnderNormalBand() && !(levelOverNormalBand()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelUnderNormalBand() && !(levelOverNormalBand()) && !(valveOpen)) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && !(levelOverNormalBand()) && valveOpen) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && valveOpen) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && !(valveOpen)) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) && !(event == TransmissionReceived && initialisationEmergency()) && event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && !(levelUnderNormalBand()) && !(levelOverNormalBand()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && initialisationEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && !(steamBoilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && !(nonLevelUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && physicalUnitsReadyReceived() && nonLevelUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelOverNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && !(levelOverNormalBand()) && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && levelUnderNormalBand() && !(levelOverNormalBand()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == TransmissionReceived && !(initialisationEmergency()) && steamBoilerWaitingReceived() && !(physicalUnitsReadyReceived()) && !(levelUnderNormalBand()) && !(levelOverNormalBand()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && operationEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelUnderNormalBand() && !(levelOverNormalBand())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelUnderNormalBand() && !(levelOverNormalBand())) && !(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelOverNormalBand()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelOverNormalBand()) && !(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelUnderNormalBand() && !(levelOverNormalBand())) && !(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && !(levelUnderNormalBand()) && !(levelOverNormalBand())) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && operationEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelUnderNormalBand() && !(levelOverNormalBand())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && levelOverNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed()) && !(levelUnderNormalBand()) && !(levelOverNormalBand())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && operationEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) && !(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelOverNormalBand()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelOverNormalBand()) && !(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) && !(event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) && !(event == TransmissionReceived && operationEmergency()) && event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && !(levelUnderNormalBand()) && !(levelOverNormalBand())) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && operationEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && levelUnitFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && levelOverNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && !(operationEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed() && !(levelUnderNormalBand()) && !(levelOverNormalBand())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && rescueEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && rescueEmergency()) && event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && rescueEmergency()) && event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && rescueEmergency()) && event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) && !(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && rescueEmergency()) && event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelOverNormalBand()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelOverNormalBand()) && !(event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) && !(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) && !(event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) && !(event == TransmissionReceived && rescueEmergency()) && event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && !(levelUnderNormalBand()) && !(levelOverNormalBand())) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && rescueEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && !(nonLevelUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == TransmissionReceived && !(rescueEmergency()) && !(levelUnitFailed()) && nonLevelUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelUnderNormalBand() && !(levelOverNormalBand())) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && levelOverNormalBand()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == TransmissionReceived && !(rescueEmergency()) && levelUnitFailed() && !(levelUnderNormalBand()) && !(levelOverNormalBand())) {
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
