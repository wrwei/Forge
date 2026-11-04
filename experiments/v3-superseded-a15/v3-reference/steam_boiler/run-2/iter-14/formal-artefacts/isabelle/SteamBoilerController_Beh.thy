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
 


enumtype Evt = messagesReceived | modeMessage | programReady | drainValve | switchPumps 
 


enumtype OperatingMode = MODE_INITIALIZATION | MODE_NORMAL | MODE_DEGRADED | MODE_RESCUE | MODE_EMERGENCY_STOP

enumtype PumpId = PUMP1 | PUMP2 | PUMP3 | PUMP4

enumtype PumpSwitch = ALL_ON | ALL_OFF

enumtype ValveSetting = VALVE_CLOSED | VALVE_OPEN

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end

text \<open> function definition \<close>

consts emergencyDemanded :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts physicalUnitsReadyReceived :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelDeviceSound :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts otherUnitsSound :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts boilerWaitingSeen :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelAboveNormalBand :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelBelowNormalBand :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
  programReadySent :: "bool"
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
                      \<and> (st = INITIALIZATION \<longrightarrow> listens = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived})
                      \<and> (st = NORMAL \<longrightarrow> listens = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived})
                      \<and> (st = RESCUE \<longrightarrow> listens = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived})
                      \<and> (st = EMERGENCY_STOP \<longrightarrow> listens = {messagesReceived})
                                                                                                    \<and> (st = INITIALIZATION \<longrightarrow> (offered = {} \<or> offered = {messagesReceived}))
                                                                                                    \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {messagesReceived}))
                                                                                                    \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {messagesReceived}))
                                                                                                    \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {messagesReceived}))
                                                                                                    \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {} \<or> offered = {messagesReceived}))
                                                                                                    \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr  @ [State INITIALIZATION]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent)) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> \<not>(levelDeviceSound() \<and> otherUnitsSound()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> \<not>physicalUnitsReadyReceived() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> \<not>boilerWaitingSeen() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event drainValve] @ [State INITIALIZATION]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event drainValve]@ [Event switchPumps] @ [State INITIALIZATION]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> levelDeviceSound() \<and> otherUnitsSound()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> \<not>(levelDeviceSound() \<and> otherUnitsSound())) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> \<not>physicalUnitsReadyReceived()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> \<not>boilerWaitingSeen()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelBelowNormalBand()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,programReadySent\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event drainValve]@ [Event switchPumps]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= NORMAL \<and> (emergencyDemanded() \<or> physicalUnitsReadyReceived()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= NORMAL \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= NORMAL \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= NORMAL \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event switchPumps] @ [State NORMAL]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= NORMAL \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event switchPumps] @ [State NORMAL]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= NORMAL \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelBelowNormalBand()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= DEGRADED \<and> (emergencyDemanded() \<or> physicalUnitsReadyReceived()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= DEGRADED \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= DEGRADED \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= DEGRADED \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event switchPumps] @ [State DEGRADED]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= DEGRADED \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event switchPumps] @ [State DEGRADED]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= DEGRADED \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelBelowNormalBand()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= RESCUE \<and> (emergencyDemanded() \<or> physicalUnitsReadyReceived()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= RESCUE \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= RESCUE \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= RESCUE \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event switchPumps] @ [State RESCUE]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= RESCUE \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage]@ [Event switchPumps] @ [State RESCUE]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= RESCUE \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelBelowNormalBand()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived, messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation EMERGENCY_STOPToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}" 
  pre "st= EMERGENCY_STOP \<and> messagesReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event messagesReceived]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {messagesReceived}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}"
  pre "st= INITIALIZATION \<and> \<not>(\<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent)) \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> \<not>(levelDeviceSound() \<and> otherUnitsSound()) \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> \<not>physicalUnitsReadyReceived() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> \<not>boilerWaitingSeen() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> levelDeviceSound() \<and> otherUnitsSound()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> physicalUnitsReadyReceived() \<and> \<not>(levelDeviceSound() \<and> otherUnitsSound())) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> programReadySent \<and> \<not>physicalUnitsReadyReceived()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> \<not>boilerWaitingSeen()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> \<not>(\<not>emergencyDemanded() \<and> \<not>(physicalUnitsReadyReceived() \<and> \<not>programReadySent) \<and> \<not>programReadySent \<and> boilerWaitingSeen() \<and> levelBelowNormalBand()) \<and> messagesReceived \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}"
  pre "st= NORMAL \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived()) \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelBelowNormalBand()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> messagesReceived \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}"
  pre "st= DEGRADED \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived()) \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelBelowNormalBand()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> messagesReceived \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}"
  pre "st= RESCUE \<and> \<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived()) \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand() \<and> messagesReceived \<in> offered) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> levelDeviceSound() \<and> \<not>otherUnitsSound()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelBelowNormalBand()) \<and> \<not>(\<not>((emergencyDemanded() \<or> physicalUnitsReadyReceived())) \<and> \<not>levelDeviceSound() \<and> levelAboveNormalBand() \<and> \<not>levelBelowNormalBand()) \<and> messagesReceived \<in> offered)"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_EMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {messagesReceived}}"
  pre "st= EMERGENCY_STOP \<and> \<not>(messagesReceived \<in> offered)"
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
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToNORMAL INITIALIZATIONToDEGRADED INITIALIZATIONToINITIALIZATION INITIALIZATIONToINITIALIZATION_1 INITIALIZATIONToINITIALIZATION_2 INITIALIZATIONToINITIALIZATION_3 INITIALIZATIONToINITIALIZATION_4 NORMALToEMERGENCY_STOP NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 EMERGENCY_STOPToEMERGENCY_STOP Idle_INITIALIZATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE Idle_EMERGENCY_STOP


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALIZATION_inv [hoare_lemmas]: "InitialToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToNORMAL_inv [hoare_lemmas]: "INITIALIZATIONToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToDEGRADED_inv [hoare_lemmas]: "INITIALIZATIONToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_1_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_2_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_3_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_4_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves SteamBoilerController_inv"
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
  
lemma "INITIALIZATIONToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves R1 under SteamBoilerController_inv"
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
