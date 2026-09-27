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

consts odist :: " int \<Rightarrow> real"
consts tcpaTo :: " int \<Rightarrow> real"
consts hdist :: " int \<Rightarrow> real"
consts sqrt :: " real \<Rightarrow> real"
consts cdaTo :: " int \<Rightarrow> real"
consts vdist :: " int \<Rightarrow> real"
consts minsafedist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticobshorizdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticobsdfltvertdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticobsvertdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts closestDynamicIndex :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts closestStaticIndex :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts nsVel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts ewVel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts rateOfClimb :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts depth_fn :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

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
  st::"St"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"   (* FORK: STATIC. The events state `st` listens
                            for. Written by every operation's update to the
                            target state's awaited set; pinned per state by the
                            invariant below. A property OF THE AUTOMATON. *)
  offered :: "Evt set"   (* FORK: DYNAMIC. The events the environment
                            offers THIS step. Written by no operation, pinned by
                            no invariant conjunct: a free input. Read by trigger
                            presence conjuncts (C2) and absence conjuncts.
                            A property OF THE ENVIRONMENT, not of the automaton. *)
  where inv:
    "tr \<noteq> []
                      \<and> (st = OCM \<longrightarrow> listens = {reqMOM, reqVel, reqHdng})
                      \<and> (st = MOM \<longrightarrow> listens = {endTask, reqHCM, reqOCM})
                      \<and> (st = HCM \<longrightarrow> listens = {reqOCM})
                      \<and> (st = CAM \<longrightarrow> listens = {reqOCM}) \<and> offered = listens"

subsection \<open> Operations \<close>

zoperation InitialToOCM =
  over LreController
  pre "st= initial"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation OCMToOCM =
  over LreController
  pre "st= OCM \<and> reqVel \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqVel]@ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation OCMToOCM_1 =
  over LreController
  pre "st= OCM \<and> reqHdng \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqHdng]@ [Event advHdng] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation OCMToMOM =
  over LreController
  pre "st= OCM \<and> vel\<le>1.0 \<and> \<not>inOpez \<and> odist(cdyn)>1.0 \<and> odist(cstc)>1.0 \<and> reqMOM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqMOM]@ [Event advVel] @ [State MOM]
         ,listens\<Zprime> = {endTask, reqHCM, reqOCM}
         ,offered\<Zprime> = {endTask, reqHCM, reqOCM}
         ]"
        
zoperation MOMToOCM =
  over LreController
  pre "st= MOM \<and> inOpez"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation MOMToCAM =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> cda<minsafedist() \<and> tcpa\<ge>0.0"
  update "[st\<Zprime>= CAM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr  @ [State CAM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToHCM =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticobshorizdist()"
  update "[st\<Zprime>= HCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToHCM_1 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>(hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticobshorizdist()) \<and> vdist(cstc)\<le>staticobsdfltvertdist()"
  update "[st\<Zprime>= HCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToHCM_2 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>(hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticobshorizdist()) \<and> \<not>vdist(cstc)\<le>staticobsdfltvertdist() \<and> vvel\<ge>1.0 \<and> vdist(cstc)\<le>staticobsvertdist()"
  update "[st\<Zprime>= HCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToOCM_1 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>(hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticobshorizdist()) \<and> \<not>vdist(cstc)\<le>staticobsdfltvertdist() \<and> \<not>(vvel\<ge>1.0 \<and> vdist(cstc)\<le>staticobsvertdist()) \<and> reqOCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation MOMToOCM_2 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>(hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticobshorizdist()) \<and> \<not>vdist(cstc)\<le>staticobsdfltvertdist() \<and> \<not>(vvel\<ge>1.0 \<and> vdist(cstc)\<le>staticobsvertdist()) \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation MOMToHCM_3 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>(hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticobshorizdist()) \<and> \<not>vdist(cstc)\<le>staticobsdfltvertdist() \<and> \<not>(vvel\<ge>1.0 \<and> vdist(cstc)\<le>staticobsvertdist()) \<and> reqHCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= HCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqHCM]@ [Event advVel] @ [State HCM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = {reqOCM}
         ]"
        
zoperation HCMToOCM =
  over LreController
  pre "st= HCM \<and> inOpez"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr  @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation HCMToCAM =
  over LreController
  pre "st= HCM \<and> \<not>inOpez \<and> cda<minsafedist() \<and> tcpa\<ge>0.0"
  update "[st\<Zprime>= CAM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr  @ [State CAM]
         ,listens\<Zprime> = {reqOCM}
         ,offered\<Zprime> = {reqOCM}
         ]"
        
zoperation HCMToMOM =
  over LreController
  pre "st= HCM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>hdist(cstc)\<le>staticobshorizdist() \<and> \<not>vdist(cstc)\<le>staticobsvertdist()"
  update "[st\<Zprime>= MOM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event advVel] @ [State MOM]
         ,listens\<Zprime> = {endTask, reqHCM, reqOCM}
         ,offered\<Zprime> = {endTask, reqHCM, reqOCM}
         ]"
        
zoperation HCMToOCM_1 =
  over LreController
  pre "st= HCM \<and> \<not>inOpez \<and> \<not>(cda<minsafedist() \<and> tcpa\<ge>0.0) \<and> \<not>(\<not>hdist(cstc)\<le>staticobshorizdist() \<and> \<not>vdist(cstc)\<le>staticobsvertdist()) \<and> reqOCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation CAMToOCM =
  over LreController
  pre "st= CAM \<and> \<not>cda<minsafedist()"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event advVel] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
         ]"
        
zoperation CAMToOCM_1 =
  over LreController
  pre "st= CAM \<and> cda<minsafedist() \<and> reqOCM \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= OCM
         ,cdyn\<Zprime> = closestDynamicIndex()
         ,tcpa\<Zprime> = tcpaTo((closestDynamicIndex()))
         ,cda\<Zprime> = cdaTo((closestDynamicIndex()))
         ,cstc\<Zprime> = closestStaticIndex()
         ,hvel\<Zprime> = sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))
         ,vvel\<Zprime> = rateOfClimb()
         ,vel\<Zprime> = sqrt((((sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel())))) * (sqrt(((nsVel() * nsVel()) + (ewVel() * ewVel()))))) + ((rateOfClimb()) * (rateOfClimb()))))
         ,inOpez\<Zprime> = (odist((closestStaticIndex()))\<le>minsafedist() \<or> depth_fn()\<le>0.0)
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,listens\<Zprime> = {reqMOM, reqVel, reqHdng}
         ,offered\<Zprime> = {reqMOM, reqVel, reqHdng}
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
  operations  InitialToOCM OCMToOCM OCMToOCM_1 OCMToMOM MOMToOCM MOMToCAM MOMToHCM MOMToHCM_1 MOMToHCM_2 MOMToOCM_1 MOMToOCM_2 MOMToHCM_3 HCMToOCM HCMToCAM HCMToMOM HCMToOCM_1 CAMToOCM CAMToOCM_1


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes LreController_inv"
  by zpog_full

lemma InitialToOCM_inv [hoare_lemmas]: "InitialToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma OCMToOCM_inv [hoare_lemmas]: "OCMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma OCMToOCM_1_inv [hoare_lemmas]: "OCMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma OCMToMOM_inv [hoare_lemmas]: "OCMToMOM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_inv [hoare_lemmas]: "MOMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToCAM_inv [hoare_lemmas]: "MOMToCAM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_inv [hoare_lemmas]: "MOMToHCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_1_inv [hoare_lemmas]: "MOMToHCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_2_inv [hoare_lemmas]: "MOMToHCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_1_inv [hoare_lemmas]: "MOMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_2_inv [hoare_lemmas]: "MOMToOCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_3_inv [hoare_lemmas]: "MOMToHCM_3() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_inv [hoare_lemmas]: "HCMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToCAM_inv [hoare_lemmas]: "HCMToCAM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToMOM_inv [hoare_lemmas]: "HCMToMOM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_1_inv [hoare_lemmas]: "HCMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma CAMToOCM_inv [hoare_lemmas]: "CAMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma CAMToOCM_1_inv [hoare_lemmas]: "CAMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToMOM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToCAM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_3() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToCAM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToMOM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "LreController_axioms = True"

lemma LreController_deadlock_free: "LreController_axioms  \<Longrightarrow> deadlock_free LreControllerMachine"
  unfolding LreControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc insertCI)
  (* TACTIC (2026-09-16, supersedes the hasPayloadDomain fork): the closing
     call is `by (metis St.exhaust_disc <used-enum>.exhaust_disc ... insertCI)`.
     Under the invariant conjunct `offered = listens` the residual goal after
     `apply deadlock_free` is a finite case analysis over St plus every enum a
     precondition compares (e.g. `sts = noGas` puts Status in the disjunction);
     each such enum's exhaust_disc lemma is supplied, computed from the emitted
     preconditions. Measured 2026-09-16: sranger 17/17 obligations (21s), lre
     39/39 (119s), chem 17/17 (30s -- without Status.exhaust_disc the same call
     is a >900s timeout). The earlier auto branch existed for a payload
     existential measured against a goal that was FALSE before this fix; with the true
     goal, metis with the computed fact list closes the payload machine too. *)
end
