// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype OperatingMode = MODE_INITIALIZATION | MODE_NORMAL | MODE_DEGRADED | MODE_RESCUE | MODE_EMERGENCY_STOP
datatype PumpId = PUMP1 | PUMP2 | PUMP3 | PUMP4
datatype PumpSwitch = ALL_ON | ALL_OFF
datatype ValveSetting = VALVE_CLOSED | VALVE_OPEN
datatype InputEvent = NoEvent | MessagesReceived

// Static constants
const CONTROL_DELAY: int := 2
const NORMAL_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 10.0
const STEAM_INCREASE_U1: real := 4.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const MAXIMAL_M2: real := 800.0
const STEAM_DECREASE_U2: real := 4.0
const MINIMAL_M1: real := 150.0
const STEAM_MAX_W: real := 25.0
const ESTIMATION_SLICES: int := 100
const STOP_REPETITIONS: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var programReadySent: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function levelBelowNormalBand(): bool
        reads this
    function levelAboveNormalBand(): bool
        reads this
    function boilerWaitingSeen(): bool
        reads this
    function levelDeviceSound(): bool
        reads this
    function otherUnitsSound(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function emergencyDemanded(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   soundHigh := (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())
    //   levelFailureNoEmergency := !(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()
    //   readyAberrant := physicalUnitsReadyReceived() && !programReadySent
    //   operatingEmergency := emergencyDemanded() || physicalUnitsReadyReceived()
    //   soundNoEmergency := !(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()
    //   initOtherwise := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand())
    //   unitsReadyAllSound := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())
    //   degradedOtherwise := !(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))
    //   rescueOtherwise := !(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))
    //   adjustLow := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand()
    //   estimatedLow := (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand()
    //   degradedHigh := (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())
    //   levelAboveN2 := levelAboveNormalBand() && !levelBelowNormalBand()
    //   unitsAllSound := levelDeviceSound() && otherUnitsSound()
    //   normalOtherwise := !(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))
    //   unitsReadySomeDefective := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())
    //   adjustHigh := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())
    //   initCalm := !emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)
    //   unitsReadyAwaited := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()
    //   unitFailureNoEmergency := !(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()
    //   estimatedHigh := (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())
    //   soundLow := (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand()
    //   degradedLow := (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand()
    //   initIdle := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()

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
        programReadySent := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand()) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand())) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand()) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && (levelAboveNormalBand() && !levelBelowNormalBand())) && !((!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelBelowNormalBand())) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) && !(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())))) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) && !(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())))) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())))) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) && !(event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())))) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && ((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand()))) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && otherUnitsSound()) && !(!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelBelowNormalBand()) && !((!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && (levelAboveNormalBand() && !levelBelowNormalBand())))) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
    }

    method transitionFromEMERGENCY_STOP(event: InputEvent)
        requires mode == EMERGENCY_STOP
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (event == MessagesReceived) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
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
