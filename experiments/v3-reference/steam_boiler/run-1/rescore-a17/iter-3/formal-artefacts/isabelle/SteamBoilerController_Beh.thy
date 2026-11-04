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
   
enumtype St = INITIALISATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP | initial 
 


enumtype Evt = transmission | mode | valve | openPump | closePump | programReady 
 


enumtype PumpPosition = PUMP_OFF | PUMP_ON

enumtype WaterFlow = NO_FLOW | FLOW

enumtype OperatingMode = INITIALISATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record PumpControlReading = controllerIndex :: int observation :: WaterFlow
record_default PumpControlReading
show_record PumpControlReading

record PumpReading = pumpIndex :: int position :: PumpPosition
record_default PumpReading
show_record PumpReading


text \<open> function definition \<close>

consts shutdownDemanded :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts plantWaiting :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts startUpFault :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelAboveN2 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelBelowN1 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReady :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelAtRisk :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts estimateUnreliable :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
  valveOpen :: "bool"
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
                      \<and> (st = INITIALISATION \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = EMERGENCY_STOP \<longrightarrow> listens = {transmission})
                                                                                                \<and> (st = INITIALISATION \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALISATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr  @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> (shutdownDemanded() \<or> plantWaiting() \<and> startUpFault()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelAboveN2() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,valveOpen\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event valve]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelBelowN1() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,valveOpen\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event valve]@ [Event openPump]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelBelowN1() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,valveOpen\<Zprime> = False
         ,programReadySent\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event valve]@ [Event closePump]@ [Event programReady]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_5 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,programReadySent\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event programReady]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_6 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> programReadySent \<and> \<not>unitsReady() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event programReady]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> programReadySent \<and> unitsReady() \<and> \<not>unitFailure() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> programReadySent \<and> unitsReady() \<and> unitFailure() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_7 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> (\<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk()) \<or> levelFailure() \<and> estimateUnreliable()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> (\<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk()) \<or> levelFailure() \<and> estimateUnreliable()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> (\<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk()) \<or> estimateUnreliable()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> levelFailure() \<and> levelBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> levelFailure() \<and> levelAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation EMERGENCY_STOPToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= EMERGENCY_STOP \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALISATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= INITIALISATION \<and> \<not>((shutdownDemanded() \<or> plantWaiting() \<and> startUpFault()) \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelAboveN2() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelBelowN1() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> levelBelowN1() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> \<not>programReadySent \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> programReadySent \<and> \<not>unitsReady() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> programReadySent \<and> unitsReady() \<and> \<not>unitFailure() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> plantWaiting() \<and> \<not>startUpFault() \<and> programReadySent \<and> unitsReady() \<and> unitFailure() \<and> transmission \<in> offered) \<and> \<not>(transmission \<in> offered)"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= NORMAL \<and> \<not>((\<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk()) \<or> levelFailure() \<and> estimateUnreliable()) \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(transmission \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= DEGRADED \<and> \<not>((\<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk()) \<or> levelFailure() \<and> estimateUnreliable()) \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> levelFailure() \<and> \<not>estimateUnreliable() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(transmission \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= RESCUE \<and> \<not>((\<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk()) \<or> estimateUnreliable()) \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> \<not>unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> unitFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> \<not>levelFailure() \<and> unitFailure() \<and> \<not>levelBelowN1() \<and> \<not>levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> levelFailure() \<and> levelBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>shutdownDemanded() \<and> \<not>levelAtRisk() \<and> \<not>estimateUnreliable() \<and> levelFailure() \<and> levelAboveN2() \<and> transmission \<in> offered) \<and> \<not>(transmission \<in> offered)"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_EMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= EMERGENCY_STOP \<and> \<not>(transmission \<in> offered)"
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
  operations  InitialToINITIALISATION INITIALISATIONToEMERGENCY_STOP INITIALISATIONToINITIALISATION INITIALISATIONToINITIALISATION_1 INITIALISATIONToINITIALISATION_2 INITIALISATIONToINITIALISATION_3 INITIALISATIONToINITIALISATION_4 INITIALISATIONToINITIALISATION_5 INITIALISATIONToINITIALISATION_6 INITIALISATIONToNORMAL INITIALISATIONToDEGRADED INITIALISATIONToINITIALISATION_7 NORMALToEMERGENCY_STOP NORMALToRESCUE NORMALToRESCUE_1 NORMALToRESCUE_2 NORMALToDEGRADED NORMALToDEGRADED_1 NORMALToDEGRADED_2 NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToRESCUE DEGRADEDToRESCUE_1 DEGRADEDToRESCUE_2 DEGRADEDToNORMAL DEGRADEDToNORMAL_1 DEGRADEDToNORMAL_2 DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToNORMAL RESCUEToNORMAL_1 RESCUEToNORMAL_2 RESCUEToDEGRADED RESCUEToDEGRADED_1 RESCUEToDEGRADED_2 RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 EMERGENCY_STOPToEMERGENCY_STOP Idle_INITIALISATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE Idle_EMERGENCY_STOP


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALISATION_inv [hoare_lemmas]: "InitialToINITIALISATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALISATIONToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_1_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_2_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_3_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_4_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_4 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_5_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_5 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_6_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_6 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToNORMAL_inv [hoare_lemmas]: "INITIALISATIONToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToDEGRADED_inv [hoare_lemmas]: "INITIALISATIONToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_7_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_7 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_inv [hoare_lemmas]: "NORMALToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_1_inv [hoare_lemmas]: "NORMALToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_2_inv [hoare_lemmas]: "NORMALToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_inv [hoare_lemmas]: "NORMALToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_1_inv [hoare_lemmas]: "NORMALToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_2_inv [hoare_lemmas]: "NORMALToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
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

lemma DEGRADEDToRESCUE_1_inv [hoare_lemmas]: "DEGRADEDToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToRESCUE_2_inv [hoare_lemmas]: "DEGRADEDToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_inv [hoare_lemmas]: "DEGRADEDToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_1_inv [hoare_lemmas]: "DEGRADEDToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_2_inv [hoare_lemmas]: "DEGRADEDToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
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

lemma RESCUEToNORMAL_1_inv [hoare_lemmas]: "RESCUEToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToNORMAL_2_inv [hoare_lemmas]: "RESCUEToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_inv [hoare_lemmas]: "RESCUEToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_1_inv [hoare_lemmas]: "RESCUEToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_2_inv [hoare_lemmas]: "RESCUEToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_inv [hoare_lemmas]: "RESCUEToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_1_inv [hoare_lemmas]: "RESCUEToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_2_inv [hoare_lemmas]: "RESCUEToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma EMERGENCY_STOPToEMERGENCY_STOP_inv [hoare_lemmas]: "EMERGENCY_STOPToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_INITIALISATION_inv [hoare_lemmas]: "Idle_INITIALISATION (nxt) preserves SteamBoilerController_inv"
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

lemma "InitialToINITIALISATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_4 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_5 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_6 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_7 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
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
  
lemma "DEGRADEDToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
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
  
lemma "RESCUEToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "EMERGENCY_STOPToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_INITIALISATION (nxt) preserves R1 under SteamBoilerController_inv"
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
