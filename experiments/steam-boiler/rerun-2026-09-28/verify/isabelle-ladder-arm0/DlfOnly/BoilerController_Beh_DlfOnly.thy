theory BoilerController_Beh_DlfOnly
imports "Z_Machines.Z_Machine"
begin
(* isolation: ZERO lemmas - elaboration-only skeleton (zstore+records+zops+zmachine) *)

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the BoilerController state machine in Z Machine notations.\<close>

notation undefined ("???")

subsection \<open> type definition \<close>

datatype ('s, 'e) tag =
  State (ofState: 's) | Event (ofEvent: 'e)

abbreviation "is_Event x \<equiv> \<not> is_State x"

type_synonym ('s, 'e) rctrace = "('s, 'e) tag list"

definition wf_rcstore :: "('s, 'e) rctrace \<Rightarrow> 's \<Rightarrow> 's option \<Rightarrow> bool" where
[z_defs]: "wf_rcstore tr st final = (
     length(tr) > 0 
   \<and> tr ! ((length tr) -1) = State st 
   \<and> (final \<noteq> None \<longrightarrow> (\<forall>i<length tr. tr ! i = State (the final) \<longrightarrow> i= (length tr) -1)) 
   \<and> (filter is_State tr) ! (length (filter is_State tr) -1) = State  st)"
   
enumtype St = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP | initial 
 


enumtype Evt = modeMessage | valve | openPump | programReady | closePump 
 


instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record PumpReport = statePresent :: bool running :: bool flowPresent :: bool flows :: bool pumpRepaired :: bool controlRepaired :: bool pumpFailureAck :: bool controlFailureAck :: bool
record_default PumpReport
show_record PumpReport

record TransmissionData = stopRequest :: bool waiting :: bool unitsReady :: bool levelPresent :: bool level :: real steamPresent :: bool steam :: real pump1 :: PumpReport pump2 :: PumpReport pump3 :: PumpReport pump4 :: PumpReport levelRepaired :: bool steamRepaired :: bool levelFailureAck :: bool steamFailureAck :: bool aberrant :: bool
record_default TransmissionData
show_record TransmissionData


text \<open> function definition \<close>

consts pumpInflow :: " int \<Rightarrow> real"
consts baseLevelLow :: " real \<Rightarrow> real"
consts baseLevelHigh :: " real \<Rightarrow> real"
consts maxSteamOutPerCycle :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts minPumpInPerCycle :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts minSteamOutPerCycle :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts maxPumpInPerCycle :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts m1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts m2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts transmissionBroken :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stopCount :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stopRepeatLimit :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts initialSteamDefect :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelDeviceBroken :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamDeviceBroken :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts anyControlUnitBroken :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts anyNonLevelUnitBroken :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts allUnitsSound :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts n1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts n2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts plantAnnounced :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReadySeen :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore BoilerController =
  transmissionFailed :: "bool"
  stopThresholdReached :: "bool"
  initSteamDefect :: "bool"
  levelDeviceFailed :: "bool"
  steamDeviceFailed :: "bool"
  controlUnitFailed :: "bool"
  nonLevelUnitFailed :: "bool"
  everyUnitSound :: "bool"
  levelRiskM1M2 :: "bool"
  levelBelowN1 :: "bool"
  levelAboveN2 :: "bool"
  plantWaiting :: "bool"
  unitsReady :: "bool"
  initDrainNeeded :: "bool"
  initFillNeeded :: "bool"
  initReadyPhase :: "bool"
  initExitAllSound :: "bool"
  initExitDegraded :: "bool"
  rescueRepairToDegraded :: "bool"
  levelLow :: "real"
  levelHigh :: "real"
  projLow :: "real"
  projHigh :: "real"
  riskBelowM1 :: "bool"
  riskAboveM2 :: "bool"
  p :: "real"
  st::"St"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"   (* STATIC. The events state `st` listens
                            for. Written by every operation's update to the
                            target state's awaited set; pinned per state by the
                            invariant below. A property OF THE AUTOMATON. *)
  offered :: "Evt set"   (* DYNAMIC. The events the environment
                            offers THIS step. Written by no operation, pinned by
                            no invariant conjunct: a free input. Read by trigger
                            presence conjuncts and absence conjuncts.
                            A property OF THE ENVIRONMENT, not of the automaton. *)
  where inv:
    "tr \<noteq> []"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over BoilerController
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over BoilerController
  pre "st= INITIALIZATION \<and> transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_1 =
  over BoilerController
  pre "st= INITIALIZATION \<and> stopThresholdReached \<and> \<not>transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_2 =
  over BoilerController
  pre "st= INITIALIZATION \<and> initSteamDefect \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_3 =
  over BoilerController
  pre "st= INITIALIZATION \<and> levelDeviceFailed \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over BoilerController
  pre "st= INITIALIZATION \<and> initDrainNeeded \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect \<and> \<not>levelDeviceFailed"
  update "[st\<Zprime>= INITIALIZATION
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event valve]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_1 =
  over BoilerController
  pre "st= INITIALIZATION \<and> initFillNeeded \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect \<and> \<not>levelDeviceFailed \<and> \<not>initDrainNeeded"
  update "[st\<Zprime>= INITIALIZATION
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event openPump]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_2 =
  over BoilerController
  pre "st= INITIALIZATION \<and> initReadyPhase \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect \<and> \<not>levelDeviceFailed \<and> \<not>initDrainNeeded \<and> \<not>initFillNeeded"
  update "[st\<Zprime>= INITIALIZATION
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event programReady]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToNORMAL =
  over BoilerController
  pre "st= INITIALIZATION \<and> initExitAllSound \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect \<and> \<not>levelDeviceFailed \<and> \<not>initDrainNeeded \<and> \<not>initFillNeeded \<and> \<not>initReadyPhase"
  update "[st\<Zprime>= NORMAL
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToDEGRADED =
  over BoilerController
  pre "st= INITIALIZATION \<and> initExitDegraded \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect \<and> \<not>levelDeviceFailed \<and> \<not>initDrainNeeded \<and> \<not>initFillNeeded \<and> \<not>initReadyPhase \<and> \<not>initExitAllSound"
  update "[st\<Zprime>= DEGRADED
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_3 =
  over BoilerController
  pre "st= INITIALIZATION \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>initSteamDefect \<and> \<not>levelDeviceFailed \<and> \<not>initDrainNeeded \<and> \<not>initFillNeeded \<and> \<not>initReadyPhase \<and> \<not>initExitAllSound \<and> \<not>initExitDegraded"
  update "[st\<Zprime>= INITIALIZATION
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over BoilerController
  pre "st= NORMAL \<and> transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP_1 =
  over BoilerController
  pre "st= NORMAL \<and> stopThresholdReached \<and> \<not>transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP_2 =
  over BoilerController
  pre "st= NORMAL \<and> levelRiskM1M2 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToRESCUE =
  over BoilerController
  pre "st= NORMAL \<and> levelDeviceFailed \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2"
  update "[st\<Zprime>= RESCUE
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToDEGRADED =
  over BoilerController
  pre "st= NORMAL \<and> nonLevelUnitFailed \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed"
  update "[st\<Zprime>= DEGRADED
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToNORMAL =
  over BoilerController
  pre "st= NORMAL \<and> levelBelowN1 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed \<and> \<not>nonLevelUnitFailed"
  update "[st\<Zprime>= NORMAL
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToNORMAL_1 =
  over BoilerController
  pre "st= NORMAL \<and> levelAboveN2 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed \<and> \<not>nonLevelUnitFailed \<and> \<not>levelBelowN1"
  update "[st\<Zprime>= NORMAL
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {}
         ]"
        
zoperation NORMALToNORMAL_2 =
  over BoilerController
  pre "st= NORMAL \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed \<and> \<not>nonLevelUnitFailed \<and> \<not>levelBelowN1 \<and> \<not>levelAboveN2"
  update "[st\<Zprime>= NORMAL
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over BoilerController
  pre "st= DEGRADED \<and> transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_1 =
  over BoilerController
  pre "st= DEGRADED \<and> stopThresholdReached \<and> \<not>transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_2 =
  over BoilerController
  pre "st= DEGRADED \<and> levelRiskM1M2 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToRESCUE =
  over BoilerController
  pre "st= DEGRADED \<and> levelDeviceFailed \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2"
  update "[st\<Zprime>= RESCUE
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToNORMAL =
  over BoilerController
  pre "st= DEGRADED \<and> everyUnitSound \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed"
  update "[st\<Zprime>= NORMAL
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over BoilerController
  pre "st= DEGRADED \<and> levelBelowN1 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed \<and> \<not>everyUnitSound"
  update "[st\<Zprime>= DEGRADED
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over BoilerController
  pre "st= DEGRADED \<and> levelAboveN2 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed \<and> \<not>everyUnitSound \<and> \<not>levelBelowN1"
  update "[st\<Zprime>= DEGRADED
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over BoilerController
  pre "st= DEGRADED \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>levelRiskM1M2 \<and> \<not>levelDeviceFailed \<and> \<not>everyUnitSound \<and> \<not>levelBelowN1 \<and> \<not>levelAboveN2"
  update "[st\<Zprime>= DEGRADED
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over BoilerController
  pre "st= RESCUE \<and> transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_1 =
  over BoilerController
  pre "st= RESCUE \<and> stopThresholdReached \<and> \<not>transmissionFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_2 =
  over BoilerController
  pre "st= RESCUE \<and> steamDeviceFailed \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_3 =
  over BoilerController
  pre "st= RESCUE \<and> controlUnitFailed \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_4 =
  over BoilerController
  pre "st= RESCUE \<and> levelRiskM1M2 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed \<and> \<not>controlUnitFailed"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToNORMAL =
  over BoilerController
  pre "st= RESCUE \<and> everyUnitSound \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed \<and> \<not>controlUnitFailed \<and> \<not>levelRiskM1M2"
  update "[st\<Zprime>= NORMAL
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToDEGRADED =
  over BoilerController
  pre "st= RESCUE \<and> rescueRepairToDegraded \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed \<and> \<not>controlUnitFailed \<and> \<not>levelRiskM1M2 \<and> \<not>everyUnitSound"
  update "[st\<Zprime>= DEGRADED
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToRESCUE =
  over BoilerController
  pre "st= RESCUE \<and> levelBelowN1 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed \<and> \<not>controlUnitFailed \<and> \<not>levelRiskM1M2 \<and> \<not>everyUnitSound \<and> \<not>rescueRepairToDegraded"
  update "[st\<Zprime>= RESCUE
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over BoilerController
  pre "st= RESCUE \<and> levelAboveN2 \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed \<and> \<not>controlUnitFailed \<and> \<not>levelRiskM1M2 \<and> \<not>everyUnitSound \<and> \<not>rescueRepairToDegraded \<and> \<not>levelBelowN1"
  update "[st\<Zprime>= RESCUE
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {}
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over BoilerController
  pre "st= RESCUE \<and> \<not>transmissionFailed \<and> \<not>stopThresholdReached \<and> \<not>steamDeviceFailed \<and> \<not>controlUnitFailed \<and> \<not>levelRiskM1M2 \<and> \<not>everyUnitSound \<and> \<not>rescueRepairToDegraded \<and> \<not>levelBelowN1 \<and> \<not>levelAboveN2"
  update "[st\<Zprime>= RESCUE
         ,levelLow\<Zprime> = baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle())))
         ,levelHigh\<Zprime> = baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle())))
         ,projLow\<Zprime> = (((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle())
         ,projHigh\<Zprime> = (((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle())
         ,riskBelowM1\<Zprime> = ((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()
         ,riskAboveM2\<Zprime> = ((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()
         ,p\<Zprime> = (((pumpInflow(1) + pumpInflow(2)) + pumpInflow(3)) + pumpInflow(4))
         ,transmissionFailed\<Zprime> = transmissionBroken()
         ,stopThresholdReached\<Zprime> = stopCount()\<ge>stopRepeatLimit()
         ,initSteamDefect\<Zprime> = initialSteamDefect()
         ,levelDeviceFailed\<Zprime> = levelDeviceBroken()
         ,steamDeviceFailed\<Zprime> = steamDeviceBroken()
         ,controlUnitFailed\<Zprime> = anyControlUnitBroken()
         ,nonLevelUnitFailed\<Zprime> = anyNonLevelUnitBroken()
         ,everyUnitSound\<Zprime> = allUnitsSound()
         ,levelRiskM1M2\<Zprime> = ((((((baseLevelLow(projLow)) - maxSteamOutPerCycle()) + minPumpInPerCycle()))\<le>m1()) \<or> (((((baseLevelHigh(projHigh)) - minSteamOutPerCycle()) + maxPumpInPerCycle()))\<ge>m2()))
         ,levelBelowN1\<Zprime> = (baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()
         ,levelAboveN2\<Zprime> = (baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()
         ,plantWaiting\<Zprime> = plantAnnounced()
         ,unitsReady\<Zprime> = unitsReadySeen()
         ,initDrainNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initFillNeeded\<Zprime> = (plantAnnounced()) \<and> ((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>(unitsReadySeen())
         ,initReadyPhase\<Zprime> = (plantAnnounced()) \<and> \<not>((baseLevelLow((((levelLow - maxSteamOutPerCycle()) + minPumpInPerCycle()))))<n1()) \<and> \<not>((baseLevelHigh((((levelHigh - minSteamOutPerCycle()) + maxPumpInPerCycle()))))>n2()) \<and> \<not>(unitsReadySeen())
         ,initExitAllSound\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> (allUnitsSound())
         ,initExitDegraded\<Zprime> = (plantAnnounced()) \<and> (unitsReadySeen()) \<and> \<not>(allUnitsSound())
         ,rescueRepairToDegraded\<Zprime> = \<not>(levelDeviceBroken()) \<and> (anyNonLevelUnitBroken())
         ,tr\<Zprime> =tr @ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {}
         ]"
        

  
definition Init :: "BoilerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine BoilerControllerMachine =
  init Init
  invariant BoilerController_inv
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToEMERGENCY_STOP_1 INITIALIZATIONToEMERGENCY_STOP_2 INITIALIZATIONToEMERGENCY_STOP_3 INITIALIZATIONToINITIALIZATION INITIALIZATIONToINITIALIZATION_1 INITIALIZATIONToINITIALIZATION_2 INITIALIZATIONToNORMAL INITIALIZATIONToDEGRADED INITIALIZATIONToINITIALIZATION_3 NORMALToEMERGENCY_STOP NORMALToEMERGENCY_STOP_1 NORMALToEMERGENCY_STOP_2 NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToEMERGENCY_STOP_1 DEGRADEDToEMERGENCY_STOP_2 DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToEMERGENCY_STOP_1 RESCUEToEMERGENCY_STOP_2 RESCUEToEMERGENCY_STOP_3 RESCUEToEMERGENCY_STOP_4 RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 


subsection \<open> Structural Invariants \<close>








































subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"


  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

definition [z_defs]: "BoilerController_axioms = True"

(* isolation: deadlock_free lemma removed *)

lemma BoilerController_deadlock_free: "BoilerController_axioms  \<Longrightarrow> deadlock_free BoilerControllerMachine"
  unfolding BoilerControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc insertCI)
  (* FORK: the deadlock_free closing tactic is selected by `hasPayloadDomain`
     (set in PART 2 when this machine emits a typed-payload domain set such as
     `SeqGs`). The residual goal after `apply deadlock_free` is
       \<And>st. st = M\<^sub>1 \<or> ... \<or> guarded-disjuncts
     and St.exhaust_disc is the enumtype discriminator exhaustion lemma that
     matches the bare `st = X` disjunct for each state. Two cases:
       * has payload domain -> `using St.exhaust_disc by auto`. metis HANGS
         (>10 min) when a state's ONLY transition consumes a typed payload
         (e.g. gas-analysis Reading: `params gs_input \<in> SeqGs`), because the
         enabledness disjunct is `\<exists>gs_input \<in> SeqGs. ...`. With the set
         emitted as a [simp] `= UNIV` definition (PART 2), `auto` discharges the
         existential and closes via the bare disjunct. Verified on
         chemical_detector (gas-analysis, ~15s).
       * no payload domain -> `by (metis St.exhaust_disc)` (the ICECCS2023 LRE
         tactic). `auto` TIMES OUT (>6 min) on LRE's real-arithmetic guards --
         confirmed by regression -- so LRE-style machines keep metis.
     Tried and rejected: `(cases st; simp_all)` / `(cases st; auto)` leave the
     guarded disjunct (e.g. `thr() \<le> ins`) open because the split selects the
     guarded disjunct instead of the bare one. *)

end
