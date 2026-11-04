// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ValvePosition = VALVE_CLOSED | VALVE_OPEN
datatype FlowObservation = NO_WATER_FLOW | WATER_FLOWING
datatype ModeValue = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const PUMPS: int := 4
const NORMAL_N1: real := 400.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 10.0
const GRADIENT_U1: real := 2.0
const P: real := 10.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const T: real := 5.0
const GRADIENT_U2: real := 2.0
const W: real := 25.0
const LIMIT_M2: real := 800.0
const STEAM_MAX_W: real := 25.0
const LIMIT_M1: real := 150.0
const CYCLE_PERIOD: real := 5.0
const STOP_REPETITIONS: int := 3
const U1: real := 2.0
const U2: real := 2.0

class SteamBoilerController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function initialEmergency(): bool
        reads this
    function estimateUnreliable(): bool
        reads this
    function drainNeeded(): bool
        reads this
    function fillNeeded(): bool
        reads this
    function runningEmergency(): bool
        reads this
    function levelDeviceFailed(): bool
        reads this
    function plantWaitingSignalled(): bool
        reads this
    function startConfirmed(): bool
        reads this
    function otherUnitFailed(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   startupEmergency := initialEmergency()
    //   excessWater := drainNeeded()
    //   operatingEmergency := runningEmergency()
    //   lowWater := fillNeeded()
    //   levelEstimateUnreliable := estimateUnreliable()
    //   levelUnitFailed := levelDeviceFailed()
    //   nonLevelUnitFailed := otherUnitFailed()
    //   plantWaiting := plantWaitingSignalled()
    //   unitsConfirmedReady := startConfirmed()

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
        ensures old(event == TransmissionReceived && initialEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && initialEmergency()) && event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) && !(event == TransmissionReceived && initialEmergency()) && event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && otherUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && otherUnitFailed()) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) && !(event == TransmissionReceived && initialEmergency()) && event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && !(plantWaitingSignalled())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && otherUnitFailed()) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) && !(event == TransmissionReceived && initialEmergency()) && event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && drainNeeded()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && drainNeeded()) && !(event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && otherUnitFailed()) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) && !(event == TransmissionReceived && initialEmergency()) && event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && !(drainNeeded()) && fillNeeded()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && !(drainNeeded()) && fillNeeded()) && !(event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && drainNeeded()) && !(event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && otherUnitFailed()) && !(event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) && !(event == TransmissionReceived && initialEmergency()) && event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && !(drainNeeded()) && !(fillNeeded())) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && initialEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(initialEmergency()) && startConfirmed() && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && !(plantWaitingSignalled())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && drainNeeded()) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && !(drainNeeded()) && fillNeeded()) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(initialEmergency()) && !(startConfirmed()) && plantWaitingSignalled() && !(drainNeeded()) && !(fillNeeded())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && runningEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && otherUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && otherUnitFailed()) && !(event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && !(otherUnitFailed())) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && runningEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && runningEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && !(otherUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && !(otherUnitFailed())) && !(event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && otherUnitFailed()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && runningEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(levelDeviceFailed()) && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && runningEmergency()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && estimateUnreliable()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && estimateUnreliable()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && levelDeviceFailed()) && !(event == TransmissionReceived && !(runningEmergency()) && estimateUnreliable()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && !(levelDeviceFailed()) && !(otherUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && !(levelDeviceFailed()) && !(otherUnitFailed())) && !(event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && levelDeviceFailed()) && !(event == TransmissionReceived && !(runningEmergency()) && estimateUnreliable()) && !(event == TransmissionReceived && runningEmergency()) && event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && !(levelDeviceFailed()) && otherUnitFailed()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && runningEmergency()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && estimateUnreliable()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && !(levelDeviceFailed()) && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(runningEmergency()) && !(estimateUnreliable()) && !(levelDeviceFailed()) && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
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
