theory LreController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the LreController state machine in Z Machine notations.\<close>

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
   
enumtype St = OCM | MOM | HCM | CAM | initial 
 


enumtype Evt = advVel | reqOCM | endTask | reqHCM 
 


instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record Obstacle = ns_rel_dist :: real ew_rel_dist :: real obs_depth :: real obs_ns_vel :: real obs_ew_vel :: real obs_roc :: real
record_default Obstacle
show_record Obstacle


text \<open> function definition \<close>

consts ewRelDist :: " int \<Rightarrow> real"
consts hdist :: " int \<Rightarrow> real"
consts vdist :: " int \<Rightarrow> real"
consts obsNsVel :: " int \<Rightarrow> real"
consts sqrt :: " real \<Rightarrow> real"
consts odist :: " int \<Rightarrow> real"
consts nsRelDist :: " int \<Rightarrow> real"
consts obsEwVel :: " int \<Rightarrow> real"
consts minsafedist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts closestDynamicIndex :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts ns_vel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts ew_vel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts closestStaticIndex :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts rate_of_climb :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts depth_fn :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticobshorizdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticobsdfltvertdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticobsvertdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore LreController =
  inOpez :: "bool"
  hvel :: "real"
  vvel :: "real"
  vel :: "real"
  cstc :: "int"
  cdyn :: "int"
  cda :: "real"
  tcpa :: "real"
  camActive :: "bool"
  hcmActive :: "bool"
  momReturn :: "bool"
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
                      \<and> (st = MOM \<longrightarrow> listens = {reqOCM, endTask, reqHCM})
                      \<and> (st = HCM \<longrightarrow> listens = {reqOCM})
                      \<and> (st = CAM \<longrightarrow> listens = {reqOCM})
                                                                               \<and> (st = OCM \<longrightarrow> (offered = {}))
                                                                               \<and> (st = MOM \<longrightarrow> (offered = {} \<or> offered = {reqOCM} \<or> offered = {endTask} \<or> offered = {reqHCM}))
                                                                               \<and> (st = HCM \<longrightarrow> (offered = {} \<or> offered = {reqOCM}))
                                                                               \<and> (st = CAM \<longrightarrow> (offered = {} \<or> offered = {reqOCM}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToOCM =
  over LreController
  pre "st= initial"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation MOMToCAM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}}" 
  pre "st= MOM \<and> camActive"
  update "[st\<Zprime>= CAM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State CAM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOMToHCM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}}" 
  pre "st= MOM \<and> hcmActive \<and> \<not>camActive"
  update "[st\<Zprime>= HCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOMToOCM =
  over LreController
  pre "st= MOM \<and> inOpez \<and> \<not>camActive \<and> \<not>hcmActive"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation MOMToOCM_1 =
  over LreController
  pre "st= MOM \<and> \<not>camActive \<and> \<not>hcmActive \<and> \<not>inOpez \<and> reqOCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation MOMToOCM_2 =
  over LreController
  pre "st= MOM \<and> \<not>camActive \<and> \<not>hcmActive \<and> \<not>inOpez \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation MOMToHCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqOCM}}" 
  pre "st= MOM \<and> \<not>camActive \<and> \<not>hcmActive \<and> \<not>inOpez \<and> reqHCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= HCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqHCM]@ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToCAM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}}" 
  pre "st= HCM \<and> camActive"
  update "[st\<Zprime>= CAM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State CAM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToOCM =
  over LreController
  pre "st= HCM \<and> inOpez \<and> \<not>camActive"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation HCMToMOM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}, {endTask}, {reqHCM}}" 
  pre "st= HCM \<and> momReturn \<and> \<not>camActive \<and> \<not>inOpez"
  update "[st\<Zprime>= MOM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event advVel] @ [State MOM]
         ,listens\<Zprime> = {reqOCM, endTask, reqHCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToOCM_1 =
  over LreController
  pre "st= HCM \<and> \<not>camActive \<and> \<not>inOpez \<and> \<not>momReturn \<and> reqOCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation CAMToOCM =
  over LreController
  pre "st= CAM \<and> reqOCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation CAMToOCM_1 =
  over LreController
  pre "st= CAM \<and> cda\<ge>minsafedist() \<and> \<not>(reqOCM \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = ((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))
         ,cda\<Zprime> = sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))))))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))
         ,vvel\<Zprime> = rate_of_climb()
         ,vel\<Zprime> = sqrt((((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel())))) * (sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))) + ((rate_of_climb()) * (rate_of_climb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))\<ge>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        

  
zoperation Idle_MOM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}, {endTask}, {reqHCM}}"
  pre "st= MOM \<and> \<not>(camActive) \<and> \<not>(hcmActive) \<and> \<not>(inOpez) \<and> \<not>(reqOCM \<in> offered) \<and> \<not>(endTask \<in> offered) \<and> \<not>(reqHCM \<in> offered)"
  update "[st\<Zprime>= MOM
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_HCM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}}"
  pre "st= HCM \<and> \<not>(camActive) \<and> \<not>(inOpez) \<and> \<not>(momReturn) \<and> \<not>(reqOCM \<in> offered)"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_CAM =
  over LreController
  params nxt \<in> "{{}, {reqOCM}}"
  pre "st= CAM \<and> \<not>(reqOCM \<in> offered) \<and> \<not>(cda\<ge>minsafedist())"
  update "[st\<Zprime>= CAM
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "LreController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine LreControllerMachine =
  init Init
  invariant LreController_inv
  operations  InitialToOCM MOMToCAM MOMToHCM MOMToOCM MOMToOCM_1 MOMToOCM_2 MOMToHCM_1 HCMToCAM HCMToOCM HCMToMOM HCMToOCM_1 CAMToOCM CAMToOCM_1 Idle_MOM Idle_HCM Idle_CAM
  until "st = OCM"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes LreController_inv"
  by zpog_full

lemma InitialToOCM_inv [hoare_lemmas]: "InitialToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToCAM_inv [hoare_lemmas]: "MOMToCAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToHCM_inv [hoare_lemmas]: "MOMToHCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToOCM_inv [hoare_lemmas]: "MOMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_1_inv [hoare_lemmas]: "MOMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_2_inv [hoare_lemmas]: "MOMToOCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_1_inv [hoare_lemmas]: "MOMToHCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToCAM_inv [hoare_lemmas]: "HCMToCAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToOCM_inv [hoare_lemmas]: "HCMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToMOM_inv [hoare_lemmas]: "HCMToMOM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToOCM_1_inv [hoare_lemmas]: "HCMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma CAMToOCM_inv [hoare_lemmas]: "CAMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma CAMToOCM_1_inv [hoare_lemmas]: "CAMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma Idle_MOM_inv [hoare_lemmas]: "Idle_MOM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_HCM_inv [hoare_lemmas]: "Idle_HCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_CAM_inv [hoare_lemmas]: "Idle_CAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToCAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToCAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToMOM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_MOM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_HCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_CAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "LreController_axioms = True"

(* WARNING -- THIS OBLIGATION IS FALSE AS EMITTED.
   State(s) with no outgoing transition: OCM.
   Under `loop` semantics such a state offers Vis {} = deadlock, so
   deadlock_free does not hold. This is a MODEL defect (a missing tick-triggered
   self-loop in the Java), not a proof-search problem: no tactic can close it.
   Measured on chemical_detector v2 -- auto OOM 978s, metis 1825s, blast 922s,
   fastforce 923s; refuted by probe_Y3_obligation_refuted with witness
   st = OCM.
   Fix the Java per forge.assets/prompts/java_codegen_rules.txt ("EVERY MODE
   MUST HAVE AN OUTGOING TRANSITION"), re-extract, and this marker disappears. *)
lemma LreController_deadlock_free: "LreController_axioms  \<Longrightarrow> deadlock_free LreControllerMachine"
  unfolding LreControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
