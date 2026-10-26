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
 


enumtype Evt = reqVel | advVel | reqHdng | advHdng | reqMOM | reqOCM 
 


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

consts obsNsVel :: " int \<Rightarrow> real"
consts hdist :: " int \<Rightarrow> real"
consts vdist :: " int \<Rightarrow> real"
consts nsRelDist :: " int \<Rightarrow> real"
consts obsEwVel :: " int \<Rightarrow> real"
consts sqrt :: " real \<Rightarrow> real"
consts odist :: " int \<Rightarrow> real"
consts ewRelDist :: " int \<Rightarrow> real"
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
                      \<and> (st = OCM \<longrightarrow> listens = {reqMOM, reqHdng, reqVel})
                      \<and> (st = HCM \<longrightarrow> listens = {reqOCM})
                      \<and> (st = CAM \<longrightarrow> listens = {reqOCM})
                                                                               \<and> (st = OCM \<longrightarrow> (offered = {} \<or> offered = {reqMOM} \<or> offered = {reqHdng} \<or> offered = {reqVel}))
                                                                               \<and> (st = MOM \<longrightarrow> (offered = {}))
                                                                               \<and> (st = HCM \<longrightarrow> (offered = {} \<or> offered = {reqOCM}))
                                                                               \<and> (st = CAM \<longrightarrow> (offered = {} \<or> offered = {reqOCM}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToOCM =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
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
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation OCMToOCM =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
  pre "st= OCM \<and> reqVel \<in> offered \<and> offered \<subseteq> listens"
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
         ,tr\<Zprime> =tr @ [Event reqVel]@ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation OCMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
  pre "st= OCM \<and> reqHdng \<in> offered \<and> offered \<subseteq> listens"
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
         ,tr\<Zprime> =tr @ [Event reqHdng]@ [Event advHdng] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation OCMToMOM =
  over LreController
  pre "st= OCM \<and> vel\<le>1.0 \<and> \<not>inOpez \<and> odist(cdyn)>1.0 \<and> odist(cstc)>1.0 \<and> reqMOM \<in> offered \<and> offered \<subseteq> listens"
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
         ,tr\<Zprime> =tr @ [Event reqMOM]@ [Event advVel] @ [State MOM]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
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
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
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
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToMOM =
  over LreController
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
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation HCMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
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
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation CAMToOCM =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
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
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation CAMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}" 
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
         ,listens\<Zprime> = {reqMOM, reqHdng, reqVel}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_OCM =
  over LreController
  params nxt \<in> "{{}, {reqMOM}, {reqHdng}, {reqVel}}"
  pre "st= OCM \<and> \<not>(reqVel \<in> offered) \<and> \<not>(reqHdng \<in> offered) \<and> \<not>(vel\<le>1.0 \<and> \<not>inOpez \<and> odist(cdyn)>1.0 \<and> odist(cstc)>1.0 \<and> reqMOM \<in> offered)"
  update "[st\<Zprime>= OCM
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
  operations  InitialToOCM OCMToOCM OCMToOCM_1 OCMToMOM HCMToCAM HCMToOCM HCMToMOM HCMToOCM_1 CAMToOCM CAMToOCM_1 Idle_OCM Idle_HCM Idle_CAM
  until "st = MOM"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes LreController_inv"
  by zpog_full

lemma InitialToOCM_inv [hoare_lemmas]: "InitialToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToOCM_inv [hoare_lemmas]: "OCMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToOCM_1_inv [hoare_lemmas]: "OCMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToMOM_inv [hoare_lemmas]: "OCMToMOM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToCAM_inv [hoare_lemmas]: "HCMToCAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToOCM_inv [hoare_lemmas]: "HCMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToMOM_inv [hoare_lemmas]: "HCMToMOM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_1_inv [hoare_lemmas]: "HCMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma CAMToOCM_inv [hoare_lemmas]: "CAMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma CAMToOCM_1_inv [hoare_lemmas]: "CAMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_OCM_inv [hoare_lemmas]: "Idle_OCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_HCM_inv [hoare_lemmas]: "Idle_HCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_CAM_inv [hoare_lemmas]: "Idle_CAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToMOM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToCAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToMOM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_OCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_HCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_CAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "LreController_axioms = True"

(* WARNING -- THIS OBLIGATION IS FALSE AS EMITTED.
   State(s) with no outgoing transition: MOM.
   Under `loop` semantics such a state offers Vis {} = deadlock, so
   deadlock_free does not hold. This is a MODEL defect (a missing tick-triggered
   self-loop in the Java), not a proof-search problem: no tactic can close it.
   Measured on chemical_detector v2 -- auto OOM 978s, metis 1825s, blast 922s,
   fastforce 923s; refuted by probe_Y3_obligation_refuted with witness
   st = MOM.
   Fix the Java per forge.assets/prompts/java_codegen_rules.txt ("EVERY MODE
   MUST HAVE AN OUTGOING TRANSITION"), re-extract, and this marker disappears. *)
lemma LreController_deadlock_free: "LreController_axioms  \<Longrightarrow> deadlock_free LreControllerMachine"
  unfolding LreControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
