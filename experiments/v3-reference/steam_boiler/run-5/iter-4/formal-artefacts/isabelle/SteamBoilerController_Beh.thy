theory SteamBoilerController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the SteamBoilerController state machine in Z Machine notations.\<close>

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
 


enumtype Evt = transmissionReceived | mode | programReady | valve | closePump | openPump 
 


enumtype PumpPosition = PUMP_OFF | PUMP_ON

enumtype UnitCondition = SOUND | FAILED_UNACKNOWLEDGED | FAILED_ACKNOWLEDGED

enumtype ValvePosition = VALVE_CLOSED | VALVE_OPEN

enumtype WaterFlow = NOT_FLOWING | FLOWING

enumtype OperatingMode = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record PumpControlReading = controlledPump :: int flow :: WaterFlow
record_default PumpControlReading
show_record PumpControlReading

record PumpStateReading = pumpNumber :: int position :: PumpPosition
record_default PumpStateReading
show_record PumpStateReading


text \<open> function definition \<close>

consts initializationAbort :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts plantWaiting :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReady :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts otherUnitFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts aboveNormal :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts belowNormal :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts emergencyStop :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
  readyAnnounced :: "bool"
  valveOpen :: "bool"
  st::"St"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"   (* FORK: STATIC. The events state `st` listens
                            for. Written by every operation's update to the
                            target state's awaited set; pinned per state by the
                            invariant below. A property OF THE AUTOMATON. *)
  offered :: "Evt set"   (* DYNAMIC. The events the environment offers THIS
                            step. Environment model B (2026-10-20): the
                            invariant pins, per state, offered = {} or
                            offered = {e} for one listened e -- the Java step
                            consumes at most one input event. Every operation
                            sets offered' to a fresh choice `nxt` drawn from the
                            target state's allowed offers. Read by trigger
                            presence conjuncts (C2) and absence conjuncts (C1). *)
  where inv:
    "tr \<noteq> []
                      \<and> (st = INITIALIZATION \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = EMERGENCY_STOP \<longrightarrow> listens = {transmissionReceived})
                                                                                                        \<and> (st = INITIALIZATION \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr  @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> initializationAbort() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>plantWaiting() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> unitsReady() \<and> otherUnitFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> unitsReady() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> readyAnnounced \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> aboveNormal() \<and> \<not>valveOpen \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,valveOpen\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event valve]@ [Event closePump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> aboveNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> valveOpen \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,valveOpen\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event valve] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_5 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> belowNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_6 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,readyAnnounced\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> emergencyStop() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> levelFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> otherUnitFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> belowNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> aboveNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> emergencyStop() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> levelFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>otherUnitFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> belowNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> aboveNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> emergencyStop() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>levelFailed() \<and> \<not>otherUnitFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>levelFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> belowNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> aboveNormal() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation EMERGENCY_STOPToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= EMERGENCY_STOP \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= INITIALIZATION \<and> \<not>(initializationAbort() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>plantWaiting() \<and> transmissionReceived \<in> offered) \<and> \<not>(unitsReady() \<and> otherUnitFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(unitsReady() \<and> transmissionReceived \<in> offered) \<and> \<not>(readyAnnounced \<and> transmissionReceived \<in> offered) \<and> \<not>(aboveNormal() \<and> \<not>valveOpen \<and> transmissionReceived \<in> offered) \<and> \<not>(aboveNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(valveOpen \<and> transmissionReceived \<in> offered) \<and> \<not>(belowNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= NORMAL \<and> \<not>(emergencyStop() \<and> transmissionReceived \<in> offered) \<and> \<not>(levelFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(otherUnitFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(belowNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(aboveNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= DEGRADED \<and> \<not>(emergencyStop() \<and> transmissionReceived \<in> offered) \<and> \<not>(levelFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>otherUnitFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(belowNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(aboveNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= RESCUE \<and> \<not>(emergencyStop() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>levelFailed() \<and> \<not>otherUnitFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>levelFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(belowNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(aboveNormal() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_EMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= EMERGENCY_STOP \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "SteamBoilerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SteamBoilerControllerMachine =
  init Init
  invariant SteamBoilerController_inv
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToINITIALIZATION INITIALIZATIONToDEGRADED INITIALIZATIONToNORMAL INITIALIZATIONToINITIALIZATION_1 INITIALIZATIONToINITIALIZATION_2 INITIALIZATIONToINITIALIZATION_3 INITIALIZATIONToINITIALIZATION_4 INITIALIZATIONToINITIALIZATION_5 INITIALIZATIONToINITIALIZATION_6 NORMALToEMERGENCY_STOP NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 EMERGENCY_STOPToEMERGENCY_STOP Idle_INITIALIZATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE Idle_EMERGENCY_STOP


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALIZATION_inv [hoare_lemmas]: "InitialToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToDEGRADED_inv [hoare_lemmas]: "INITIALIZATIONToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToNORMAL_inv [hoare_lemmas]: "INITIALIZATIONToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_1_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_2_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_3_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_4_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_5_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_5 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_6_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_6 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_inv [hoare_lemmas]: "NORMALToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_inv [hoare_lemmas]: "NORMALToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_inv [hoare_lemmas]: "NORMALToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_1_inv [hoare_lemmas]: "NORMALToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_2_inv [hoare_lemmas]: "NORMALToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToEMERGENCY_STOP_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToRESCUE_inv [hoare_lemmas]: "DEGRADEDToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_inv [hoare_lemmas]: "DEGRADEDToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_inv [hoare_lemmas]: "DEGRADEDToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_1_inv [hoare_lemmas]: "DEGRADEDToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_2_inv [hoare_lemmas]: "DEGRADEDToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToEMERGENCY_STOP_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToNORMAL_inv [hoare_lemmas]: "RESCUEToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_inv [hoare_lemmas]: "RESCUEToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_inv [hoare_lemmas]: "RESCUEToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_1_inv [hoare_lemmas]: "RESCUEToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_2_inv [hoare_lemmas]: "RESCUEToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma EMERGENCY_STOPToEMERGENCY_STOP_inv [hoare_lemmas]: "EMERGENCY_STOPToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_INITIALIZATION_inv [hoare_lemmas]: "Idle_INITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_NORMAL_inv [hoare_lemmas]: "Idle_NORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_DEGRADED_inv [hoare_lemmas]: "Idle_DEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_RESCUE_inv [hoare_lemmas]: "Idle_RESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_EMERGENCY_STOP_inv [hoare_lemmas]: "Idle_EMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_5 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_6 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "EMERGENCY_STOPToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_INITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_NORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_DEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_RESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_EMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SteamBoilerController_axioms = True"

lemma SteamBoilerController_deadlock_free: "SteamBoilerController_axioms  \<Longrightarrow> deadlock_free SteamBoilerControllerMachine"
  unfolding SteamBoilerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
