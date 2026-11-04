// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype FlowObservation = NO_FLOW | WATER_FLOWING
datatype PumpPosition = PUMP_OFF | PUMP_ON
datatype ValvePosition = VALVE_CLOSED | VALVE_OPEN
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const NORMAL_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 10.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const SETTLED_AGE: int := 2
const STEAM_RISE_U1: real := 4.0
const STEAM_FALL_U2: real := 6.0
const LIMIT_M2: real := 900.0
const STEAM_MAX_W: real := 25.0
const LIMIT_M1: real := 100.0
const STOP_LIMIT: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var readyAnnounced: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function successiveStops(): int
        reads this
    function levelLimitRisk(): bool
        reads this
    function rescueEstimateUnreliable(): bool
        reads this
    function transmissionFailed(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelHeadingLow(): bool
        reads this
    function levelBelowNormal(): bool
        reads this
    function steamBoilerWaitingSeen(): bool
        reads this
    function otherUnitOutOfOrder(): bool
        reads this
    function steamRateNonZero(): bool
        reads this
    function levelDeviceOutOfOrder(): bool
        reads this
    function levelHeadingHigh(): bool
        reads this
    function levelAboveNormal(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   unitsReady := physicalUnitsReadyReceived()
    //   estimateUnreliable := rescueEstimateUnreliable()
    //   headingAboveN2 := levelHeadingHigh()
    //   transmissionFault := transmissionFailed()
    //   stopLimitReached := successiveStops() >= 3
    //   levelBelowN1 := levelBelowNormal()
    //   levelFailed := levelDeviceOutOfOrder()
    //   programReadySent := readyAnnounced
    //   levelAboveN2 := levelAboveNormal()
    //   plantWaiting := steamBoilerWaitingSeen()
    //   steamNonZero := steamRateNonZero()
    //   otherFailed := otherUnitOutOfOrder()
    //   limitRisk := levelLimitRisk()
    //   headingBelowN1 := levelHeadingLow()

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
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && steamRateNonZero()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && levelDeviceOutOfOrder()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && levelAboveNormal()) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && levelBelowNormal()) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && !(readyAnnounced) && !(levelAboveNormal()) && !(levelBelowNormal())) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && physicalUnitsReadyReceived() && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && steamBoilerWaitingSeen() && !(steamRateNonZero()) && !(levelDeviceOutOfOrder()) && readyAnnounced && !(physicalUnitsReadyReceived())) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionReceived) {
            mode := INITIALIZATION;
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelHeadingLow()) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelHeadingLow()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelHeadingLow()) && levelHeadingHigh()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) {
            mode := RESCUE;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && levelHeadingLow()) {
            mode := NORMAL;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder()) && !(levelHeadingLow()) && levelHeadingHigh()) {
            mode := NORMAL;
        }
        else if (event == TransmissionReceived) {
            mode := NORMAL;
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelHeadingLow()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelHeadingLow()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelHeadingLow()) && levelHeadingHigh()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && levelLimitRisk()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && rescueEstimateUnreliable()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(rescueEstimateUnreliable())) {
            mode := RESCUE;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && levelHeadingLow()) {
            mode := DEGRADED;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder() && !(levelHeadingLow()) && levelHeadingHigh()) {
            mode := DEGRADED;
        }
        else if (event == TransmissionReceived) {
            mode := DEGRADED;
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && levelLimitRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && levelHeadingLow()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && levelHeadingLow()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && levelLimitRisk()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) && !(event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) && !(event == TransmissionReceived && successiveStops() >= 3) && event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(levelHeadingLow()) && levelHeadingHigh()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && transmissionFailed()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && rescueEstimateUnreliable()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && levelLimitRisk()) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && !(otherUnitOutOfOrder())) {
            mode := NORMAL;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && !(levelDeviceOutOfOrder()) && otherUnitOutOfOrder()) {
            mode := DEGRADED;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && levelHeadingLow()) {
            mode := RESCUE;
        }
        else if (event == TransmissionReceived && !(successiveStops() >= 3) && !(transmissionFailed()) && !(rescueEstimateUnreliable()) && !(levelLimitRisk()) && levelDeviceOutOfOrder() && !(levelHeadingLow()) && levelHeadingHigh()) {
            mode := RESCUE;
        }
        else if (event == TransmissionReceived) {
            mode := RESCUE;
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
