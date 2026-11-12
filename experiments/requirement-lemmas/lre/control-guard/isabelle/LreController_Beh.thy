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
 


enumtype Evt = reqVel | advVel | reqHdng | advHdng | reqMOM | reqOCM | endTask | reqHCM 
 


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
consts odist :: " int \<Rightarrow> real"
consts nsRelDist :: " int \<Rightarrow> real"
consts obsNsVel :: " int \<Rightarrow> real"
consts obsEwVel :: " int \<Rightarrow> real"
consts vdist :: " int \<Rightarrow> real"
consts sqrt :: " real \<Rightarrow> real"
definition minsafedist :: "unit \<Rightarrow> real" where "minsafedist u = 1"  (* named constant; value from the model *)
consts closestDynamicIndex :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts ns_vel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts ew_vel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts closestStaticIndex :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts rate_of_climb :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts depth_fn :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition staticobshorizdist :: "unit \<Rightarrow> real" where "staticobshorizdist u = 1"  (* named constant; value from the model *)
definition staticobsdfltvertdist :: "unit \<Rightarrow> real" where "staticobsdfltvertdist u = 1"  (* named constant; value from the model *)
definition staticobsvertdist :: "unit \<Rightarrow> real" where "staticobsvertdist u = 1"  (* named constant; value from the model *)

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
                      \<and> (st = OCM \<longrightarrow> listens = {reqVel, reqHdng, reqMOM})
                      \<and> (st = MOM \<longrightarrow> listens = {endTask, reqOCM, reqHCM})
                      \<and> (st = HCM \<longrightarrow> listens = {reqOCM})
                      \<and> (st = CAM \<longrightarrow> listens = {reqOCM})
                                                                               \<and> (st = OCM \<longrightarrow> (offered = {} \<or> offered = {reqVel} \<or> offered = {reqHdng} \<or> offered = {reqMOM}))
                                                                               \<and> (st = MOM \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {reqOCM} \<or> offered = {reqHCM}))
                                                                               \<and> (st = HCM \<longrightarrow> (offered = {} \<or> offered = {reqOCM}))
                                                                               \<and> (st = CAM \<longrightarrow> (offered = {} \<or> offered = {reqOCM}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToOCM =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation OCMToOCM =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqVel]@ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation OCMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqHdng]@ [Event advHdng] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation OCMToMOM =
  over LreController
  params nxt \<in> "{{}, {endTask}, {reqOCM}, {reqHCM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqMOM]@ [Event advVel] @ [State MOM]
         ,listens\<Zprime> = {endTask, reqOCM, reqHCM}
         ,offered\<Zprime> = nxt
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOMToOCM =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOMToOCM_2 =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State CAM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToOCM =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToMOM =
  over LreController
  params nxt \<in> "{{}, {endTask}, {reqOCM}, {reqHCM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event advVel] @ [State MOM]
         ,listens\<Zprime> = {endTask, reqOCM, reqHCM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation HCMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation CAMToOCM =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation CAMToOCM_1 =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}" 
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
         ,camActive\<Zprime> = (sqrt((((nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (nsRelDist((closestDynamicIndex())) + ((obsNsVel((closestDynamicIndex())) - ns_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))) + ((ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel()))))))) * (ewRelDist((closestDynamicIndex())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))))))))<minsafedist() \<and> (((- ((nsRelDist((closestDynamicIndex())) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + (ewRelDist((closestDynamicIndex())) * (obsEwVel((closestDynamicIndex())) - ew_vel())))) / (((obsNsVel((closestDynamicIndex())) - ns_vel()) * (obsNsVel((closestDynamicIndex())) - ns_vel())) + ((obsEwVel((closestDynamicIndex())) - ew_vel()) * (obsEwVel((closestDynamicIndex())) - ew_vel())))))>0.0
         ,hcmActive\<Zprime> = (((sqrt(((ns_vel() * ns_vel()) + (ew_vel() * ew_vel()))))\<ge>1.0 \<and> hdist((closestStaticIndex()))\<le>staticobshorizdist() \<or> vdist((closestStaticIndex()))\<le>staticobsdfltvertdist()) \<or> (rate_of_climb())\<ge>1.0 \<and> vdist((closestStaticIndex()))\<le>staticobsvertdist())
         ,momReturn\<Zprime> = hdist((closestStaticIndex()))>staticobshorizdist() \<and> vdist((closestStaticIndex()))>staticobsvertdist()
         ,tr\<Zprime> =tr @ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqVel, reqHdng, reqMOM}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_OCM =
  over LreController
  params nxt \<in> "{{}, {reqVel}, {reqHdng}, {reqMOM}}"
  pre "st= OCM \<and> \<not>(reqVel \<in> offered) \<and> \<not>(reqHdng \<in> offered) \<and> \<not>(vel\<le>1.0 \<and> \<not>inOpez \<and> odist(cdyn)>1.0 \<and> odist(cstc)>1.0 \<and> reqMOM \<in> offered)"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_MOM =
  over LreController
  params nxt \<in> "{{}, {endTask}, {reqOCM}, {reqHCM}}"
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
  operations  InitialToOCM OCMToOCM OCMToOCM_1 OCMToMOM MOMToCAM MOMToHCM MOMToOCM MOMToOCM_1 MOMToOCM_2 MOMToHCM_1 HCMToCAM HCMToOCM HCMToMOM HCMToOCM_1 CAMToOCM CAMToOCM_1 Idle_OCM Idle_MOM Idle_HCM Idle_CAM


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes LreController_inv"
  by zpog_full

lemma InitialToOCM_inv [hoare_lemmas]: "InitialToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToOCM_inv [hoare_lemmas]: "OCMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToOCM_1_inv [hoare_lemmas]: "OCMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToMOM_inv [hoare_lemmas]: "OCMToMOM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToCAM_inv [hoare_lemmas]: "MOMToCAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToHCM_inv [hoare_lemmas]: "MOMToHCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToOCM_inv [hoare_lemmas]: "MOMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToOCM_1_inv [hoare_lemmas]: "MOMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToOCM_2_inv [hoare_lemmas]: "MOMToOCM_2 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOMToHCM_1_inv [hoare_lemmas]: "MOMToHCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToCAM_inv [hoare_lemmas]: "HCMToCAM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToOCM_inv [hoare_lemmas]: "HCMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToMOM_inv [hoare_lemmas]: "HCMToMOM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma HCMToOCM_1_inv [hoare_lemmas]: "HCMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma CAMToOCM_inv [hoare_lemmas]: "CAMToOCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma CAMToOCM_1_inv [hoare_lemmas]: "CAMToOCM_1 (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_OCM_inv [hoare_lemmas]: "Idle_OCM (nxt) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

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

lemma "InitialToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToMOM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToCAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_2 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToCAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToMOM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM_1 (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_OCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_MOM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_HCM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "Idle_CAM (nxt) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "LreController_axioms = True"

lemma LreController_deadlock_free: "LreController_axioms  \<Longrightarrow> deadlock_free LreControllerMachine"
  unfolding LreControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+

subsection \<open> Requirement-derived obligations (reference specification) \<close>

zexpr Bind is "(camActive \<longleftrightarrow> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0))) \<and> (hcmActive \<longleftrightarrow> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist())))) \<and> (momReturn \<longleftrightarrow> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()))"

lemma Bind_establish: "H{True} InitialToOCM (nxt) {_. Bind}"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_InitialToOCM: "InitialToOCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_OCMToOCM: "OCMToOCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_OCMToOCM_1: "OCMToOCM_1 (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_OCMToMOM: "OCMToMOM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_MOMToCAM: "MOMToCAM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_MOMToHCM: "MOMToHCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_MOMToOCM: "MOMToOCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_MOMToOCM_1: "MOMToOCM_1 (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_MOMToOCM_2: "MOMToOCM_2 (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_MOMToHCM_1: "MOMToHCM_1 (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_HCMToCAM: "HCMToCAM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_HCMToOCM: "HCMToOCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_HCMToMOM: "HCMToMOM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_HCMToOCM_1: "HCMToOCM_1 (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_CAMToOCM: "CAMToOCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_CAMToOCM_1: "CAMToOCM_1 (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_Idle_OCM: "Idle_OCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_Idle_MOM: "Idle_MOM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_Idle_HCM: "Idle_HCM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)
lemma Bind_Idle_CAM: "Idle_CAM (nxt) preserves Bind"
  by (zpog_full; simp add: disj_assoc)

zexpr Pre_Init is "st = initial \<and> True"
zexpr Adm_Init is "st \<in> {OCM}"
lemma Req_Init_InitialToOCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_OCMToOCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_OCMToOCM_1: "H{Pre_Init \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_OCMToMOM: "H{Pre_Init \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MOMToCAM: "H{Pre_Init \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MOMToHCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MOMToOCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MOMToOCM_1: "H{Pre_Init \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MOMToOCM_2: "H{Pre_Init \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MOMToHCM_1: "H{Pre_Init \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_HCMToCAM: "H{Pre_Init \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_HCMToOCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_HCMToMOM: "H{Pre_Init \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_HCMToOCM_1: "H{Pre_Init \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_CAMToOCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_CAMToOCM_1: "H{Pre_Init \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_OCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_MOM: "H{Pre_Init \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_HCM: "H{Pre_Init \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_CAM: "H{Pre_Init \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_Init}"
  by (zpog_full; simp)

zexpr Pre_OCM_none_M is "st = OCM \<and> offered = {} \<and> ((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_none_M is "st \<in> {OCM}"
lemma Req_OCM_none_M_InitialToOCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_OCMToOCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_OCMToOCM_1: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_OCMToMOM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_MOMToCAM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_MOMToHCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_MOMToOCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_MOMToOCM_1: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_MOMToOCM_2: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_MOMToHCM_1: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_HCMToCAM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_HCMToOCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_HCMToMOM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_HCMToOCM_1: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_CAMToOCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_CAMToOCM_1: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_Idle_OCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_Idle_MOM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_Idle_HCM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)
lemma Req_OCM_none_M_Idle_CAM: "H{Pre_OCM_none_M \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_none_M}"
  by (zpog_full; simp)

zexpr Pre_OCM_none_m is "st = OCM \<and> offered = {} \<and> \<not>((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_none_m is "st \<in> {OCM}"
lemma Req_OCM_none_m_InitialToOCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_OCMToOCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_OCMToOCM_1: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_OCMToMOM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_MOMToCAM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_MOMToHCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_MOMToOCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_MOMToOCM_1: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_MOMToOCM_2: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_MOMToHCM_1: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_HCMToCAM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_HCMToOCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_HCMToMOM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_HCMToOCM_1: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_CAMToOCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_CAMToOCM_1: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_Idle_OCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_Idle_MOM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_Idle_HCM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)
lemma Req_OCM_none_m_Idle_CAM: "H{Pre_OCM_none_m \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_none_m}"
  by (zpog_full; simp)

zexpr Pre_OCM_reqHdng_M is "st = OCM \<and> offered = {reqHdng} \<and> ((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_reqHdng_M is "st \<in> {OCM}"
lemma Req_OCM_reqHdng_M_InitialToOCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_OCMToOCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_OCMToOCM_1: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_OCMToMOM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_MOMToCAM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_MOMToHCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_MOMToOCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_MOMToOCM_1: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_MOMToOCM_2: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_MOMToHCM_1: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_HCMToCAM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_HCMToOCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_HCMToMOM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_HCMToOCM_1: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_CAMToOCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_CAMToOCM_1: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_Idle_OCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_Idle_MOM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_Idle_HCM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_M_Idle_CAM: "H{Pre_OCM_reqHdng_M \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_reqHdng_M}"
  by (zpog_full; simp)

zexpr Pre_OCM_reqHdng_m is "st = OCM \<and> offered = {reqHdng} \<and> \<not>((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_reqHdng_m is "st \<in> {OCM}"
lemma Req_OCM_reqHdng_m_InitialToOCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_OCMToOCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_OCMToOCM_1: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_OCMToMOM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_MOMToCAM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_MOMToHCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_MOMToOCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_MOMToOCM_1: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_MOMToOCM_2: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_MOMToHCM_1: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_HCMToCAM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_HCMToOCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_HCMToMOM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_HCMToOCM_1: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_CAMToOCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_CAMToOCM_1: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_Idle_OCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_Idle_MOM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_Idle_HCM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqHdng_m_Idle_CAM: "H{Pre_OCM_reqHdng_m \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_reqHdng_m}"
  by (zpog_full; simp)

zexpr Pre_OCM_reqMOM_M is "st = OCM \<and> offered = {reqMOM} \<and> ((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_reqMOM_M is "st \<in> {MOM}"
lemma Req_OCM_reqMOM_M_InitialToOCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_OCMToOCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_OCMToOCM_1: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_OCMToMOM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_MOMToCAM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_MOMToHCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_MOMToOCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_MOMToOCM_1: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_MOMToOCM_2: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_MOMToHCM_1: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_HCMToCAM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_HCMToOCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_HCMToMOM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_HCMToOCM_1: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_CAMToOCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_CAMToOCM_1: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_Idle_OCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_Idle_MOM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_Idle_HCM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_M_Idle_CAM: "H{Pre_OCM_reqMOM_M \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_reqMOM_M}"
  by (zpog_full; simp)

zexpr Pre_OCM_reqMOM_m is "st = OCM \<and> offered = {reqMOM} \<and> \<not>((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_reqMOM_m is "st \<in> {OCM}"
lemma Req_OCM_reqMOM_m_InitialToOCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_OCMToOCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_OCMToOCM_1: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_OCMToMOM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_MOMToCAM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_MOMToHCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_MOMToOCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_MOMToOCM_1: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_MOMToOCM_2: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_MOMToHCM_1: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_HCMToCAM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_HCMToOCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_HCMToMOM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_HCMToOCM_1: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_CAMToOCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_CAMToOCM_1: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_Idle_OCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_Idle_MOM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_Idle_HCM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqMOM_m_Idle_CAM: "H{Pre_OCM_reqMOM_m \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_reqMOM_m}"
  by (zpog_full; simp)

zexpr Pre_OCM_reqVel_M is "st = OCM \<and> offered = {reqVel} \<and> ((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_reqVel_M is "st \<in> {OCM}"
lemma Req_OCM_reqVel_M_InitialToOCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_OCMToOCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_OCMToOCM_1: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_OCMToMOM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_MOMToCAM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_MOMToHCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_MOMToOCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_MOMToOCM_1: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_MOMToOCM_2: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_MOMToHCM_1: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_HCMToCAM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_HCMToOCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_HCMToMOM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_HCMToOCM_1: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_CAMToOCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_CAMToOCM_1: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_Idle_OCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_Idle_MOM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_Idle_HCM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_M_Idle_CAM: "H{Pre_OCM_reqVel_M \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_reqVel_M}"
  by (zpog_full; simp)

zexpr Pre_OCM_reqVel_m is "st = OCM \<and> offered = {reqVel} \<and> \<not>((vel\<le>1.0) \<and> \<not>(inOpez) \<and> (odist(cdyn)>1.0) \<and> (odist(cstc)>1.0))"
zexpr Adm_OCM_reqVel_m is "st \<in> {OCM}"
lemma Req_OCM_reqVel_m_InitialToOCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_OCMToOCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_OCMToOCM_1: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_OCMToMOM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_MOMToCAM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_MOMToHCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_MOMToOCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_MOMToOCM_1: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_MOMToOCM_2: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_MOMToHCM_1: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_HCMToCAM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_HCMToOCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_HCMToMOM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_HCMToOCM_1: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_CAMToOCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_CAMToOCM_1: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_Idle_OCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_Idle_MOM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_Idle_HCM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)
lemma Req_OCM_reqVel_m_Idle_CAM: "H{Pre_OCM_reqVel_m \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_OCM_reqVel_m}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_BCP is "st = MOM \<and> offered = {} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_none_BCP is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_none_BCP_InitialToOCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_OCMToOCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_OCMToOCM_1: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_OCMToMOM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_MOMToCAM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_MOMToHCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_MOMToOCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_MOMToOCM_1: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_MOMToOCM_2: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_MOMToHCM_1: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_HCMToCAM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_HCMToOCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_HCMToMOM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_HCMToOCM_1: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_CAMToOCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_CAMToOCM_1: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_Idle_OCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_Idle_MOM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_Idle_HCM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCP_Idle_CAM: "H{Pre_MOM_none_BCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_BCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_BCp is "st = MOM \<and> offered = {} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_none_BCp is "st \<in> {CAM, HCM}"
lemma Req_MOM_none_BCp_InitialToOCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_OCMToOCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_OCMToOCM_1: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_OCMToMOM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_MOMToCAM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_MOMToHCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_MOMToOCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_MOMToOCM_1: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_MOMToOCM_2: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_MOMToHCM_1: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_HCMToCAM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_HCMToOCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_HCMToMOM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_HCMToOCM_1: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_CAMToOCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_CAMToOCM_1: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_Idle_OCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_Idle_MOM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_Idle_HCM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_BCp_Idle_CAM: "H{Pre_MOM_none_BCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_BCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_BcP is "st = MOM \<and> offered = {} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_none_BcP is "st \<in> {HCM, OCM}"
lemma Req_MOM_none_BcP_InitialToOCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_OCMToOCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_OCMToOCM_1: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_OCMToMOM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_MOMToCAM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_MOMToHCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_MOMToOCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_MOMToOCM_1: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_MOMToOCM_2: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_MOMToHCM_1: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_HCMToCAM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_HCMToOCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_HCMToMOM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_HCMToOCM_1: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_CAMToOCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_CAMToOCM_1: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_Idle_OCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_Idle_MOM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_Idle_HCM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_BcP_Idle_CAM: "H{Pre_MOM_none_BcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_BcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_Bcp is "st = MOM \<and> offered = {} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_none_Bcp is "st \<in> {HCM}"
lemma Req_MOM_none_Bcp_InitialToOCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_OCMToOCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_OCMToOCM_1: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_OCMToMOM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_MOMToCAM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_MOMToHCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_MOMToOCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_MOMToOCM_1: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_MOMToOCM_2: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_MOMToHCM_1: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_HCMToCAM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_HCMToOCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_HCMToMOM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_HCMToOCM_1: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_CAMToOCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_CAMToOCM_1: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_Idle_OCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_Idle_MOM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_Idle_HCM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_Bcp_Idle_CAM: "H{Pre_MOM_none_Bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_Bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_bCP is "st = MOM \<and> offered = {} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_none_bCP is "st \<in> {CAM, OCM}"
lemma Req_MOM_none_bCP_InitialToOCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_OCMToOCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_OCMToOCM_1: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_OCMToMOM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_MOMToCAM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_MOMToHCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_MOMToOCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_MOMToOCM_1: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_MOMToOCM_2: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_MOMToHCM_1: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_HCMToCAM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_HCMToOCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_HCMToMOM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_HCMToOCM_1: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_CAMToOCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_CAMToOCM_1: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_Idle_OCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_Idle_MOM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_Idle_HCM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCP_Idle_CAM: "H{Pre_MOM_none_bCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_bCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_bCp is "st = MOM \<and> offered = {} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_none_bCp is "st \<in> {CAM}"
lemma Req_MOM_none_bCp_InitialToOCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_OCMToOCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_OCMToOCM_1: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_OCMToMOM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_MOMToCAM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_MOMToHCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_MOMToOCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_MOMToOCM_1: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_MOMToOCM_2: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_MOMToHCM_1: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_HCMToCAM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_HCMToOCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_HCMToMOM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_HCMToOCM_1: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_CAMToOCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_CAMToOCM_1: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_Idle_OCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_Idle_MOM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_Idle_HCM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bCp_Idle_CAM: "H{Pre_MOM_none_bCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_bCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_bcP is "st = MOM \<and> offered = {} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_none_bcP is "st \<in> {OCM}"
lemma Req_MOM_none_bcP_InitialToOCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_OCMToOCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_OCMToOCM_1: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_OCMToMOM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_MOMToCAM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_MOMToHCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_MOMToOCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_MOMToOCM_1: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_MOMToOCM_2: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_MOMToHCM_1: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_HCMToCAM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_HCMToOCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_HCMToMOM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_HCMToOCM_1: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_CAMToOCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_CAMToOCM_1: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_Idle_OCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_Idle_MOM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_Idle_HCM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcP_Idle_CAM: "H{Pre_MOM_none_bcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_bcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_none_bcp is "st = MOM \<and> offered = {} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_none_bcp is "st \<in> {MOM}"
lemma Req_MOM_none_bcp_InitialToOCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_OCMToOCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_OCMToOCM_1: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_OCMToMOM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_MOMToCAM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_MOMToHCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_MOMToOCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_MOMToOCM_1: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_MOMToOCM_2: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_MOMToHCM_1: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_HCMToCAM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_HCMToOCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_HCMToMOM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_HCMToOCM_1: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_CAMToOCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_CAMToOCM_1: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_Idle_OCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_Idle_MOM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_Idle_HCM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_none_bcp_Idle_CAM: "H{Pre_MOM_none_bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_none_bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_BCP is "st = MOM \<and> offered = {endTask} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_endTask_BCP is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_endTask_BCP_InitialToOCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_OCMToOCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_OCMToOCM_1: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_OCMToMOM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_MOMToCAM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_MOMToHCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_MOMToOCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_MOMToOCM_1: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_MOMToOCM_2: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_MOMToHCM_1: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_HCMToCAM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_HCMToOCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_HCMToMOM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_HCMToOCM_1: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_CAMToOCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_CAMToOCM_1: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_Idle_OCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_Idle_MOM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_Idle_HCM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCP_Idle_CAM: "H{Pre_MOM_endTask_BCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_BCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_BCp is "st = MOM \<and> offered = {endTask} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_endTask_BCp is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_endTask_BCp_InitialToOCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_OCMToOCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_OCMToOCM_1: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_OCMToMOM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_MOMToCAM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_MOMToHCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_MOMToOCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_MOMToOCM_1: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_MOMToOCM_2: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_MOMToHCM_1: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_HCMToCAM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_HCMToOCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_HCMToMOM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_HCMToOCM_1: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_CAMToOCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_CAMToOCM_1: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_Idle_OCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_Idle_MOM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_Idle_HCM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BCp_Idle_CAM: "H{Pre_MOM_endTask_BCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_BCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_BcP is "st = MOM \<and> offered = {endTask} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_endTask_BcP is "st \<in> {HCM, OCM}"
lemma Req_MOM_endTask_BcP_InitialToOCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_OCMToOCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_OCMToOCM_1: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_OCMToMOM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_MOMToCAM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_MOMToHCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_MOMToOCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_MOMToOCM_1: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_MOMToOCM_2: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_MOMToHCM_1: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_HCMToCAM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_HCMToOCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_HCMToMOM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_HCMToOCM_1: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_CAMToOCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_CAMToOCM_1: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_Idle_OCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_Idle_MOM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_Idle_HCM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_BcP_Idle_CAM: "H{Pre_MOM_endTask_BcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_BcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_Bcp is "st = MOM \<and> offered = {endTask} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_endTask_Bcp is "st \<in> {HCM, OCM}"
lemma Req_MOM_endTask_Bcp_InitialToOCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_OCMToOCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_OCMToOCM_1: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_OCMToMOM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_MOMToCAM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_MOMToHCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_MOMToOCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_MOMToOCM_1: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_MOMToOCM_2: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_MOMToHCM_1: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_HCMToCAM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_HCMToOCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_HCMToMOM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_HCMToOCM_1: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_CAMToOCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_CAMToOCM_1: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_Idle_OCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_Idle_MOM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_Idle_HCM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_Bcp_Idle_CAM: "H{Pre_MOM_endTask_Bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_Bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_bCP is "st = MOM \<and> offered = {endTask} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_endTask_bCP is "st \<in> {CAM, OCM}"
lemma Req_MOM_endTask_bCP_InitialToOCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_OCMToOCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_OCMToOCM_1: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_OCMToMOM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_MOMToCAM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_MOMToHCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_MOMToOCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_MOMToOCM_1: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_MOMToOCM_2: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_MOMToHCM_1: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_HCMToCAM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_HCMToOCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_HCMToMOM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_HCMToOCM_1: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_CAMToOCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_CAMToOCM_1: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_Idle_OCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_Idle_MOM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_Idle_HCM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCP_Idle_CAM: "H{Pre_MOM_endTask_bCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_bCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_bCp is "st = MOM \<and> offered = {endTask} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_endTask_bCp is "st \<in> {CAM, OCM}"
lemma Req_MOM_endTask_bCp_InitialToOCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_OCMToOCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_OCMToOCM_1: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_OCMToMOM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_MOMToCAM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_MOMToHCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_MOMToOCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_MOMToOCM_1: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_MOMToOCM_2: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_MOMToHCM_1: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_HCMToCAM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_HCMToOCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_HCMToMOM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_HCMToOCM_1: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_CAMToOCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_CAMToOCM_1: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_Idle_OCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_Idle_MOM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_Idle_HCM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bCp_Idle_CAM: "H{Pre_MOM_endTask_bCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_bCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_bcP is "st = MOM \<and> offered = {endTask} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_endTask_bcP is "st \<in> {OCM}"
lemma Req_MOM_endTask_bcP_InitialToOCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_OCMToOCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_OCMToOCM_1: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_OCMToMOM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_MOMToCAM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_MOMToHCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_MOMToOCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_MOMToOCM_1: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_MOMToOCM_2: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_MOMToHCM_1: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_HCMToCAM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_HCMToOCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_HCMToMOM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_HCMToOCM_1: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_CAMToOCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_CAMToOCM_1: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_Idle_OCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_Idle_MOM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_Idle_HCM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcP_Idle_CAM: "H{Pre_MOM_endTask_bcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_bcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_endTask_bcp is "st = MOM \<and> offered = {endTask} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_endTask_bcp is "st \<in> {OCM}"
lemma Req_MOM_endTask_bcp_InitialToOCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_OCMToOCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_OCMToOCM_1: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_OCMToMOM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_MOMToCAM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_MOMToHCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_MOMToOCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_MOMToOCM_1: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_MOMToOCM_2: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_MOMToHCM_1: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_HCMToCAM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_HCMToOCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_HCMToMOM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_HCMToOCM_1: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_CAMToOCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_CAMToOCM_1: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_Idle_OCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_Idle_MOM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_Idle_HCM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_endTask_bcp_Idle_CAM: "H{Pre_MOM_endTask_bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_endTask_bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_BCP is "st = MOM \<and> offered = {reqHCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqHCM_BCP is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_reqHCM_BCP_InitialToOCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_OCMToOCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_OCMToOCM_1: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_OCMToMOM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_MOMToCAM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_MOMToHCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_MOMToOCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_MOMToOCM_1: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_MOMToOCM_2: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_MOMToHCM_1: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_HCMToCAM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_HCMToOCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_HCMToMOM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_HCMToOCM_1: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_CAMToOCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_CAMToOCM_1: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_Idle_OCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_Idle_MOM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_Idle_HCM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCP_Idle_CAM: "H{Pre_MOM_reqHCM_BCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_BCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_BCp is "st = MOM \<and> offered = {reqHCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqHCM_BCp is "st \<in> {CAM, HCM}"
lemma Req_MOM_reqHCM_BCp_InitialToOCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_OCMToOCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_OCMToOCM_1: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_OCMToMOM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_MOMToCAM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_MOMToHCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_MOMToOCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_MOMToOCM_1: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_MOMToOCM_2: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_MOMToHCM_1: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_HCMToCAM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_HCMToOCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_HCMToMOM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_HCMToOCM_1: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_CAMToOCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_CAMToOCM_1: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_Idle_OCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_Idle_MOM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_Idle_HCM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BCp_Idle_CAM: "H{Pre_MOM_reqHCM_BCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_BCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_BcP is "st = MOM \<and> offered = {reqHCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqHCM_BcP is "st \<in> {HCM, OCM}"
lemma Req_MOM_reqHCM_BcP_InitialToOCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_OCMToOCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_OCMToOCM_1: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_OCMToMOM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_MOMToCAM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_MOMToHCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_MOMToOCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_MOMToOCM_1: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_MOMToOCM_2: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_MOMToHCM_1: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_HCMToCAM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_HCMToOCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_HCMToMOM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_HCMToOCM_1: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_CAMToOCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_CAMToOCM_1: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_Idle_OCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_Idle_MOM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_Idle_HCM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_BcP_Idle_CAM: "H{Pre_MOM_reqHCM_BcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_BcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_Bcp is "st = MOM \<and> offered = {reqHCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqHCM_Bcp is "st \<in> {HCM}"
lemma Req_MOM_reqHCM_Bcp_InitialToOCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_OCMToOCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_OCMToOCM_1: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_OCMToMOM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_MOMToCAM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_MOMToHCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_MOMToOCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_MOMToOCM_1: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_MOMToOCM_2: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_MOMToHCM_1: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_HCMToCAM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_HCMToOCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_HCMToMOM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_HCMToOCM_1: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_CAMToOCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_CAMToOCM_1: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_Idle_OCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_Idle_MOM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_Idle_HCM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_Bcp_Idle_CAM: "H{Pre_MOM_reqHCM_Bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_Bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_bCP is "st = MOM \<and> offered = {reqHCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqHCM_bCP is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_reqHCM_bCP_InitialToOCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_OCMToOCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_OCMToOCM_1: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_OCMToMOM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_MOMToCAM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_MOMToHCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_MOMToOCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_MOMToOCM_1: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_MOMToOCM_2: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_MOMToHCM_1: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_HCMToCAM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_HCMToOCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_HCMToMOM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_HCMToOCM_1: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_CAMToOCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_CAMToOCM_1: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_Idle_OCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_Idle_MOM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_Idle_HCM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCP_Idle_CAM: "H{Pre_MOM_reqHCM_bCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_bCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_bCp is "st = MOM \<and> offered = {reqHCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqHCM_bCp is "st \<in> {CAM, HCM}"
lemma Req_MOM_reqHCM_bCp_InitialToOCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_OCMToOCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_OCMToOCM_1: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_OCMToMOM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_MOMToCAM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_MOMToHCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_MOMToOCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_MOMToOCM_1: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_MOMToOCM_2: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_MOMToHCM_1: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_HCMToCAM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_HCMToOCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_HCMToMOM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_HCMToOCM_1: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_CAMToOCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_CAMToOCM_1: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_Idle_OCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_Idle_MOM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_Idle_HCM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bCp_Idle_CAM: "H{Pre_MOM_reqHCM_bCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_bCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_bcP is "st = MOM \<and> offered = {reqHCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqHCM_bcP is "st \<in> {HCM, OCM}"
lemma Req_MOM_reqHCM_bcP_InitialToOCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_OCMToOCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_OCMToOCM_1: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_OCMToMOM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_MOMToCAM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_MOMToHCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_MOMToOCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_MOMToOCM_1: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_MOMToOCM_2: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_MOMToHCM_1: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_HCMToCAM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_HCMToOCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_HCMToMOM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_HCMToOCM_1: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_CAMToOCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_CAMToOCM_1: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_Idle_OCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_Idle_MOM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_Idle_HCM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcP_Idle_CAM: "H{Pre_MOM_reqHCM_bcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_bcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqHCM_bcp is "st = MOM \<and> offered = {reqHCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqHCM_bcp is "st \<in> {HCM}"
lemma Req_MOM_reqHCM_bcp_InitialToOCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_OCMToOCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_OCMToOCM_1: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_OCMToMOM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_MOMToCAM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_MOMToHCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_MOMToOCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_MOMToOCM_1: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_MOMToOCM_2: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_MOMToHCM_1: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_HCMToCAM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_HCMToOCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_HCMToMOM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_HCMToOCM_1: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_CAMToOCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_CAMToOCM_1: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_Idle_OCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_Idle_MOM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_Idle_HCM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqHCM_bcp_Idle_CAM: "H{Pre_MOM_reqHCM_bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqHCM_bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_BCP is "st = MOM \<and> offered = {reqOCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqOCM_BCP is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_reqOCM_BCP_InitialToOCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_OCMToOCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_OCMToOCM_1: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_OCMToMOM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_MOMToCAM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_MOMToHCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_MOMToOCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_MOMToOCM_1: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_MOMToOCM_2: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_MOMToHCM_1: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_HCMToCAM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_HCMToOCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_HCMToMOM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_HCMToOCM_1: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_CAMToOCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_CAMToOCM_1: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_Idle_OCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_Idle_MOM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_Idle_HCM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCP_Idle_CAM: "H{Pre_MOM_reqOCM_BCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_BCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_BCp is "st = MOM \<and> offered = {reqOCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqOCM_BCp is "st \<in> {CAM, HCM, OCM}"
lemma Req_MOM_reqOCM_BCp_InitialToOCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_OCMToOCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_OCMToOCM_1: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_OCMToMOM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_MOMToCAM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_MOMToHCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_MOMToOCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_MOMToOCM_1: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_MOMToOCM_2: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_MOMToHCM_1: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_HCMToCAM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_HCMToOCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_HCMToMOM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_HCMToOCM_1: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_CAMToOCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_CAMToOCM_1: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_Idle_OCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_Idle_MOM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_Idle_HCM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BCp_Idle_CAM: "H{Pre_MOM_reqOCM_BCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_BCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_BcP is "st = MOM \<and> offered = {reqOCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqOCM_BcP is "st \<in> {HCM, OCM}"
lemma Req_MOM_reqOCM_BcP_InitialToOCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_OCMToOCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_OCMToOCM_1: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_OCMToMOM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_MOMToCAM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_MOMToHCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_MOMToOCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_MOMToOCM_1: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_MOMToOCM_2: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_MOMToHCM_1: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_HCMToCAM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_HCMToOCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_HCMToMOM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_HCMToOCM_1: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_CAMToOCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_CAMToOCM_1: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_Idle_OCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_Idle_MOM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_Idle_HCM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_BcP_Idle_CAM: "H{Pre_MOM_reqOCM_BcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_BcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_Bcp is "st = MOM \<and> offered = {reqOCM} \<and> (((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqOCM_Bcp is "st \<in> {HCM, OCM}"
lemma Req_MOM_reqOCM_Bcp_InitialToOCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_OCMToOCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_OCMToOCM_1: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_OCMToMOM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_MOMToCAM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_MOMToHCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_MOMToOCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_MOMToOCM_1: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_MOMToOCM_2: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_MOMToHCM_1: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_HCMToCAM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_HCMToOCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_HCMToMOM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_HCMToOCM_1: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_CAMToOCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_CAMToOCM_1: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_Idle_OCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_Idle_MOM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_Idle_HCM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_Bcp_Idle_CAM: "H{Pre_MOM_reqOCM_Bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_Bcp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_bCP is "st = MOM \<and> offered = {reqOCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqOCM_bCP is "st \<in> {CAM, OCM}"
lemma Req_MOM_reqOCM_bCP_InitialToOCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_OCMToOCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_OCMToOCM_1: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_OCMToMOM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_MOMToCAM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_MOMToHCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_MOMToOCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_MOMToOCM_1: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_MOMToOCM_2: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_MOMToHCM_1: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_HCMToCAM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_HCMToOCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_HCMToMOM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_HCMToOCM_1: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_CAMToOCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_CAMToOCM_1: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_Idle_OCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_Idle_MOM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_Idle_HCM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCP_Idle_CAM: "H{Pre_MOM_reqOCM_bCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_bCP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_bCp is "st = MOM \<and> offered = {reqOCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqOCM_bCp is "st \<in> {CAM, OCM}"
lemma Req_MOM_reqOCM_bCp_InitialToOCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_OCMToOCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_OCMToOCM_1: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_OCMToMOM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_MOMToCAM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_MOMToHCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_MOMToOCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_MOMToOCM_1: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_MOMToOCM_2: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_MOMToHCM_1: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_HCMToCAM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_HCMToOCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_HCMToMOM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_HCMToOCM_1: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_CAMToOCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_CAMToOCM_1: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_Idle_OCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_Idle_MOM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_Idle_HCM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bCp_Idle_CAM: "H{Pre_MOM_reqOCM_bCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_bCp}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_bcP is "st = MOM \<and> offered = {reqOCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_MOM_reqOCM_bcP is "st \<in> {OCM}"
lemma Req_MOM_reqOCM_bcP_InitialToOCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_OCMToOCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_OCMToOCM_1: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_OCMToMOM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_MOMToCAM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_MOMToHCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_MOMToOCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_MOMToOCM_1: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_MOMToOCM_2: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_MOMToHCM_1: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_HCMToCAM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_HCMToOCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_HCMToMOM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_HCMToOCM_1: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_CAMToOCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_CAMToOCM_1: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_Idle_OCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_Idle_MOM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_Idle_HCM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcP_Idle_CAM: "H{Pre_MOM_reqOCM_bcP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_bcP}"
  by (zpog_full; simp)

zexpr Pre_MOM_reqOCM_bcp is "st = MOM \<and> offered = {reqOCM} \<and> \<not>(((hvel\<ge>1.0) \<and> (hdist(cstc)\<le>staticobshorizdist())) \<or> (vdist(cstc)\<le>staticobsdfltvertdist()) \<or> ((vvel\<ge>1.0) \<and> (vdist(cstc)\<le>staticobsvertdist()))) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_MOM_reqOCM_bcp is "st \<in> {OCM}"
lemma Req_MOM_reqOCM_bcp_InitialToOCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_OCMToOCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_OCMToOCM_1: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_OCMToMOM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_MOMToCAM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_MOMToHCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_MOMToOCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_MOMToOCM_1: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_MOMToOCM_2: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_MOMToHCM_1: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_HCMToCAM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_HCMToOCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_HCMToMOM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_HCMToOCM_1: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_CAMToOCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_CAMToOCM_1: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_Idle_OCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_Idle_MOM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_Idle_HCM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)
lemma Req_MOM_reqOCM_bcp_Idle_CAM: "H{Pre_MOM_reqOCM_bcp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_MOM_reqOCM_bcp}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_SCP is "st = HCM \<and> offered = {} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_none_SCP is "st \<in> {CAM, MOM, OCM}"
lemma Req_HCM_none_SCP_InitialToOCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_OCMToOCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_OCMToOCM_1: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_OCMToMOM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_MOMToCAM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_MOMToHCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_MOMToOCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_MOMToOCM_1: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_MOMToOCM_2: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_MOMToHCM_1: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_HCMToCAM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_HCMToOCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_HCMToMOM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_HCMToOCM_1: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_CAMToOCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_CAMToOCM_1: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_Idle_OCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_Idle_MOM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_Idle_HCM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCP_Idle_CAM: "H{Pre_HCM_none_SCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_SCP}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_SCp is "st = HCM \<and> offered = {} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_none_SCp is "st \<in> {CAM, MOM}"
lemma Req_HCM_none_SCp_InitialToOCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_OCMToOCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_OCMToOCM_1: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_OCMToMOM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_MOMToCAM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_MOMToHCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_MOMToOCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_MOMToOCM_1: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_MOMToOCM_2: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_MOMToHCM_1: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_HCMToCAM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_HCMToOCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_HCMToMOM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_HCMToOCM_1: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_CAMToOCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_CAMToOCM_1: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_Idle_OCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_Idle_MOM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_Idle_HCM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_SCp_Idle_CAM: "H{Pre_HCM_none_SCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_SCp}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_ScP is "st = HCM \<and> offered = {} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_none_ScP is "st \<in> {MOM, OCM}"
lemma Req_HCM_none_ScP_InitialToOCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_OCMToOCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_OCMToOCM_1: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_OCMToMOM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_MOMToCAM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_MOMToHCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_MOMToOCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_MOMToOCM_1: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_MOMToOCM_2: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_MOMToHCM_1: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_HCMToCAM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_HCMToOCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_HCMToMOM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_HCMToOCM_1: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_CAMToOCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_CAMToOCM_1: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_Idle_OCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_Idle_MOM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_Idle_HCM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_none_ScP_Idle_CAM: "H{Pre_HCM_none_ScP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_ScP}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_Scp is "st = HCM \<and> offered = {} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_none_Scp is "st \<in> {MOM}"
lemma Req_HCM_none_Scp_InitialToOCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_OCMToOCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_OCMToOCM_1: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_OCMToMOM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_MOMToCAM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_MOMToHCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_MOMToOCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_MOMToOCM_1: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_MOMToOCM_2: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_MOMToHCM_1: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_HCMToCAM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_HCMToOCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_HCMToMOM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_HCMToOCM_1: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_CAMToOCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_CAMToOCM_1: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_Idle_OCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_Idle_MOM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_Idle_HCM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_Scp_Idle_CAM: "H{Pre_HCM_none_Scp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_Scp}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_sCP is "st = HCM \<and> offered = {} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_none_sCP is "st \<in> {CAM, OCM}"
lemma Req_HCM_none_sCP_InitialToOCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_OCMToOCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_OCMToOCM_1: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_OCMToMOM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_MOMToCAM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_MOMToHCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_MOMToOCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_MOMToOCM_1: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_MOMToOCM_2: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_MOMToHCM_1: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_HCMToCAM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_HCMToOCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_HCMToMOM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_HCMToOCM_1: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_CAMToOCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_CAMToOCM_1: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_Idle_OCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_Idle_MOM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_Idle_HCM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCP_Idle_CAM: "H{Pre_HCM_none_sCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_sCP}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_sCp is "st = HCM \<and> offered = {} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_none_sCp is "st \<in> {CAM}"
lemma Req_HCM_none_sCp_InitialToOCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_OCMToOCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_OCMToOCM_1: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_OCMToMOM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_MOMToCAM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_MOMToHCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_MOMToOCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_MOMToOCM_1: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_MOMToOCM_2: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_MOMToHCM_1: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_HCMToCAM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_HCMToOCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_HCMToMOM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_HCMToOCM_1: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_CAMToOCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_CAMToOCM_1: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_Idle_OCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_Idle_MOM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_Idle_HCM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_none_sCp_Idle_CAM: "H{Pre_HCM_none_sCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_sCp}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_scP is "st = HCM \<and> offered = {} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_none_scP is "st \<in> {OCM}"
lemma Req_HCM_none_scP_InitialToOCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_OCMToOCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_OCMToOCM_1: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_OCMToMOM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_MOMToCAM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_MOMToHCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_MOMToOCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_MOMToOCM_1: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_MOMToOCM_2: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_MOMToHCM_1: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_HCMToCAM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_HCMToOCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_HCMToMOM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_HCMToOCM_1: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_CAMToOCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_CAMToOCM_1: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_Idle_OCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_Idle_MOM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_Idle_HCM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)
lemma Req_HCM_none_scP_Idle_CAM: "H{Pre_HCM_none_scP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_scP}"
  by (zpog_full; simp)

zexpr Pre_HCM_none_scp is "st = HCM \<and> offered = {} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_none_scp is "st \<in> {HCM}"
lemma Req_HCM_none_scp_InitialToOCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_OCMToOCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_OCMToOCM_1: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_OCMToMOM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_MOMToCAM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_MOMToHCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_MOMToOCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_MOMToOCM_1: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_MOMToOCM_2: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_MOMToHCM_1: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_HCMToCAM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_HCMToOCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_HCMToMOM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_HCMToOCM_1: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_CAMToOCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_CAMToOCM_1: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_Idle_OCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_Idle_MOM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_Idle_HCM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)
lemma Req_HCM_none_scp_Idle_CAM: "H{Pre_HCM_none_scp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_none_scp}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_SCP is "st = HCM \<and> offered = {reqOCM} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_reqOCM_SCP is "st \<in> {CAM, MOM, OCM}"
lemma Req_HCM_reqOCM_SCP_InitialToOCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_OCMToOCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_OCMToOCM_1: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_OCMToMOM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_MOMToCAM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_MOMToHCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_MOMToOCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_MOMToOCM_1: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_MOMToOCM_2: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_MOMToHCM_1: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_HCMToCAM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_HCMToOCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_HCMToMOM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_HCMToOCM_1: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_CAMToOCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_CAMToOCM_1: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_Idle_OCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_Idle_MOM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_Idle_HCM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCP_Idle_CAM: "H{Pre_HCM_reqOCM_SCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_SCP}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_SCp is "st = HCM \<and> offered = {reqOCM} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_reqOCM_SCp is "st \<in> {CAM, MOM, OCM}"
lemma Req_HCM_reqOCM_SCp_InitialToOCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_OCMToOCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_OCMToOCM_1: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_OCMToMOM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_MOMToCAM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_MOMToHCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_MOMToOCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_MOMToOCM_1: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_MOMToOCM_2: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_MOMToHCM_1: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_HCMToCAM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_HCMToOCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_HCMToMOM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_HCMToOCM_1: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_CAMToOCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_CAMToOCM_1: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_Idle_OCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_Idle_MOM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_Idle_HCM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_SCp_Idle_CAM: "H{Pre_HCM_reqOCM_SCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_SCp}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_ScP is "st = HCM \<and> offered = {reqOCM} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_reqOCM_ScP is "st \<in> {MOM, OCM}"
lemma Req_HCM_reqOCM_ScP_InitialToOCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_OCMToOCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_OCMToOCM_1: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_OCMToMOM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_MOMToCAM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_MOMToHCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_MOMToOCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_MOMToOCM_1: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_MOMToOCM_2: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_MOMToHCM_1: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_HCMToCAM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_HCMToOCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_HCMToMOM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_HCMToOCM_1: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_CAMToOCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_CAMToOCM_1: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_Idle_OCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_Idle_MOM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_Idle_HCM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_ScP_Idle_CAM: "H{Pre_HCM_reqOCM_ScP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_ScP}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_Scp is "st = HCM \<and> offered = {reqOCM} \<and> (hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_reqOCM_Scp is "st \<in> {MOM, OCM}"
lemma Req_HCM_reqOCM_Scp_InitialToOCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_OCMToOCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_OCMToOCM_1: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_OCMToMOM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_MOMToCAM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_MOMToHCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_MOMToOCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_MOMToOCM_1: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_MOMToOCM_2: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_MOMToHCM_1: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_HCMToCAM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_HCMToOCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_HCMToMOM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_HCMToOCM_1: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_CAMToOCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_CAMToOCM_1: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_Idle_OCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_Idle_MOM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_Idle_HCM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_Scp_Idle_CAM: "H{Pre_HCM_reqOCM_Scp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_Scp}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_sCP is "st = HCM \<and> offered = {reqOCM} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_reqOCM_sCP is "st \<in> {CAM, OCM}"
lemma Req_HCM_reqOCM_sCP_InitialToOCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_OCMToOCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_OCMToOCM_1: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_OCMToMOM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_MOMToCAM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_MOMToHCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_MOMToOCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_MOMToOCM_1: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_MOMToOCM_2: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_MOMToHCM_1: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_HCMToCAM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_HCMToOCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_HCMToMOM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_HCMToOCM_1: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_CAMToOCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_CAMToOCM_1: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_Idle_OCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_Idle_MOM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_Idle_HCM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCP_Idle_CAM: "H{Pre_HCM_reqOCM_sCP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_sCP}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_sCp is "st = HCM \<and> offered = {reqOCM} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> ((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_reqOCM_sCp is "st \<in> {CAM, OCM}"
lemma Req_HCM_reqOCM_sCp_InitialToOCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_OCMToOCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_OCMToOCM_1: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_OCMToMOM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_MOMToCAM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_MOMToHCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_MOMToOCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_MOMToOCM_1: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_MOMToOCM_2: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_MOMToHCM_1: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_HCMToCAM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_HCMToOCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_HCMToMOM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_HCMToOCM_1: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_CAMToOCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_CAMToOCM_1: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_Idle_OCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_Idle_MOM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_Idle_HCM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_sCp_Idle_CAM: "H{Pre_HCM_reqOCM_sCp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_sCp}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_scP is "st = HCM \<and> offered = {reqOCM} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> (inOpez)"
zexpr Adm_HCM_reqOCM_scP is "st \<in> {OCM}"
lemma Req_HCM_reqOCM_scP_InitialToOCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_OCMToOCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_OCMToOCM_1: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_OCMToMOM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_MOMToCAM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_MOMToHCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_MOMToOCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_MOMToOCM_1: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_MOMToOCM_2: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_MOMToHCM_1: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_HCMToCAM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_HCMToOCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_HCMToMOM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_HCMToOCM_1: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_CAMToOCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_CAMToOCM_1: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_Idle_OCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_Idle_MOM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_Idle_HCM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scP_Idle_CAM: "H{Pre_HCM_reqOCM_scP \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_scP}"
  by (zpog_full; simp)

zexpr Pre_HCM_reqOCM_scp is "st = HCM \<and> offered = {reqOCM} \<and> \<not>(hdist(cstc)>staticobshorizdist() \<and> vdist(cstc)>staticobsvertdist()) \<and> \<not>((cda<minsafedist()) \<and> (tcpa\<ge>0.0)) \<and> \<not>(inOpez)"
zexpr Adm_HCM_reqOCM_scp is "st \<in> {OCM}"
lemma Req_HCM_reqOCM_scp_InitialToOCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_OCMToOCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_OCMToOCM_1: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_OCMToMOM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_MOMToCAM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_MOMToHCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_MOMToOCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_MOMToOCM_1: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_MOMToOCM_2: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_MOMToHCM_1: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_HCMToCAM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_HCMToOCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_HCMToMOM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_HCMToOCM_1: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_CAMToOCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_CAMToOCM_1: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_Idle_OCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_Idle_MOM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_Idle_HCM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)
lemma Req_HCM_reqOCM_scp_Idle_CAM: "H{Pre_HCM_reqOCM_scp \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_HCM_reqOCM_scp}"
  by (zpog_full; simp)

zexpr Pre_CAM_none_F is "st = CAM \<and> offered = {} \<and> \<not>(cda<minsafedist())"
zexpr Adm_CAM_none_F is "st \<in> {OCM}"
lemma Req_CAM_none_F_InitialToOCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_OCMToOCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_OCMToOCM_1: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_OCMToMOM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_MOMToCAM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_MOMToHCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_MOMToOCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_MOMToOCM_1: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_MOMToOCM_2: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_MOMToHCM_1: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_HCMToCAM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_HCMToOCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_HCMToMOM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_HCMToOCM_1: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_CAMToOCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_CAMToOCM_1: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_Idle_OCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_Idle_MOM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_Idle_HCM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)
lemma Req_CAM_none_F_Idle_CAM: "H{Pre_CAM_none_F \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_CAM_none_F}"
  by (zpog_full; simp)

zexpr Pre_CAM_none_f is "st = CAM \<and> offered = {} \<and> \<not>\<not>(cda<minsafedist())"
zexpr Adm_CAM_none_f is "st \<in> {CAM}"
lemma Req_CAM_none_f_InitialToOCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_OCMToOCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_OCMToOCM_1: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_OCMToMOM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_MOMToCAM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_MOMToHCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_MOMToOCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_MOMToOCM_1: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_MOMToOCM_2: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_MOMToHCM_1: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_HCMToCAM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_HCMToOCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_HCMToMOM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_HCMToOCM_1: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_CAMToOCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_CAMToOCM_1: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_Idle_OCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_Idle_MOM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_Idle_HCM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)
lemma Req_CAM_none_f_Idle_CAM: "H{Pre_CAM_none_f \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_CAM_none_f}"
  by (zpog_full; simp)

zexpr Pre_CAM_reqOCM_F is "st = CAM \<and> offered = {reqOCM} \<and> \<not>(cda<minsafedist())"
zexpr Adm_CAM_reqOCM_F is "st \<in> {OCM}"
lemma Req_CAM_reqOCM_F_InitialToOCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_OCMToOCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_OCMToOCM_1: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_OCMToMOM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_MOMToCAM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_MOMToHCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_MOMToOCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_MOMToOCM_1: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_MOMToOCM_2: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_MOMToHCM_1: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_HCMToCAM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_HCMToOCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_HCMToMOM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_HCMToOCM_1: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_CAMToOCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_CAMToOCM_1: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_Idle_OCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_Idle_MOM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_Idle_HCM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_F_Idle_CAM: "H{Pre_CAM_reqOCM_F \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_CAM_reqOCM_F}"
  by (zpog_full; simp)

zexpr Pre_CAM_reqOCM_f is "st = CAM \<and> offered = {reqOCM} \<and> \<not>\<not>(cda<minsafedist())"
zexpr Adm_CAM_reqOCM_f is "st \<in> {OCM}"
lemma Req_CAM_reqOCM_f_InitialToOCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} InitialToOCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_OCMToOCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} OCMToOCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_OCMToOCM_1: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} OCMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_OCMToMOM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} OCMToMOM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_MOMToCAM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} MOMToCAM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_MOMToHCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} MOMToHCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_MOMToOCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} MOMToOCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_MOMToOCM_1: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} MOMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_MOMToOCM_2: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} MOMToOCM_2 (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_MOMToHCM_1: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} MOMToHCM_1 (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_HCMToCAM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} HCMToCAM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_HCMToOCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} HCMToOCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_HCMToMOM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} HCMToMOM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_HCMToOCM_1: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} HCMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_CAMToOCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} CAMToOCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_CAMToOCM_1: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} CAMToOCM_1 (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_Idle_OCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} Idle_OCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_Idle_MOM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} Idle_MOM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_Idle_HCM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} Idle_HCM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)
lemma Req_CAM_reqOCM_f_Idle_CAM: "H{Pre_CAM_reqOCM_f \<and> Bind \<and> LreController_inv} Idle_CAM (nxt) {_. Adm_CAM_reqOCM_f}"
  by (zpog_full; simp)

end
