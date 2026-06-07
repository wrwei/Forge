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
record Obstacle = nsRelDist :: real ewRelDist :: real obsDepth :: real obsNsVel :: real obsEwVel :: real obsRoc :: real
record_default Obstacle
show_record Obstacle


text \<open> function definition \<close>

consts obsNsVel :: " real \<Rightarrow> real"
consts vdist :: " nat \<Rightarrow> real"
consts nsRelDist :: " real \<Rightarrow> real"
consts obsEwVel :: " real \<Rightarrow> real"
consts hdist :: " nat \<Rightarrow> real"
consts odist :: " nat \<Rightarrow> real"
consts sqrt :: " real \<Rightarrow> real"
consts ewRelDist :: " real \<Rightarrow> real"
consts minSafeDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticObsHorizDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticObsDfltVertDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts staticObsVertDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore LreController =
  inOpez :: "bool"
  hvel :: "real"
  vvel :: "real"
  vel :: "real"
  cstc :: "nat"
  cdyn :: "nat"
  cda :: "real"
  tcpa :: "real"
  st::"St"
  tr :: "(St, Evt) tag list"
  triggers:: "Evt set"
  where inv:
    "tr \<noteq> []"

subsection \<open> Operations \<close>

zoperation InitialToOCM =
  over LreController
  pre "st= initial"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr  @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation OCMToOCM =
  over LreController
  pre "st= OCM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqVel]@ [Event advVel] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation OCMToOCM_1 =
  over LreController
  pre "st= OCM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqHdng]@ [Event advHdng] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation OCMToMOM =
  over LreController
  pre "st= OCM \<and> vel\<le>1.0 \<and> \<not>inOpez \<and> odist(cdyn)>minSafeDist() \<and> odist(cstc)>minSafeDist()"
  update "[st\<Zprime>= MOM
         ,tr\<Zprime> =tr @ [Event reqMOM]@ [Event advVel] @ [State MOM]
         ,triggers\<Zprime> = {endTask, reqOCM, reqHCM}
         ]"
        
zoperation MOMToOCM =
  over LreController
  pre "st= MOM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation MOMToOCM_1 =
  over LreController
  pre "st= MOM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event advVel] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation MOMToHCM =
  over LreController
  pre "st= MOM"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event reqHCM]@ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToCAM =
  over LreController
  pre "st= MOM \<and> cda<minSafeDist() \<and> tcpa\<ge>0.0"
  update "[st\<Zprime>= CAM
         ,tr\<Zprime> =tr  @ [State CAM]
         ,triggers\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToOCM_2 =
  over LreController
  pre "st= MOM \<and> inOpez"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr  @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation MOMToHCM_1 =
  over LreController
  pre "st= MOM \<and> hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticObsHorizDist()"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToHCM_2 =
  over LreController
  pre "st= MOM \<and> vdist(cstc)\<le>staticObsDfltVertDist()"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {reqOCM}
         ]"
        
zoperation MOMToHCM_3 =
  over LreController
  pre "st= MOM \<and> vvel\<ge>1.0 \<and> vdist(cstc)\<le>staticObsVertDist()"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {reqOCM}
         ]"
        
zoperation HCMToOCM =
  over LreController
  pre "st= HCM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation HCMToCAM =
  over LreController
  pre "st= HCM \<and> cda<minSafeDist() \<and> tcpa\<ge>0.0"
  update "[st\<Zprime>= CAM
         ,tr\<Zprime> =tr  @ [State CAM]
         ,triggers\<Zprime> = {reqOCM}
         ]"
        
zoperation HCMToOCM_1 =
  over LreController
  pre "st= HCM \<and> inOpez"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr  @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation HCMToMOM =
  over LreController
  pre "st= HCM \<and> hdist(cstc)>staticObsHorizDist() \<and> vdist(cstc)>staticObsVertDist()"
  update "[st\<Zprime>= MOM
         ,tr\<Zprime> =tr @ [Event advVel] @ [State MOM]
         ,triggers\<Zprime> = {endTask, reqOCM, reqHCM}
         ]"
        
zoperation CAMToOCM =
  over LreController
  pre "st= CAM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        
zoperation CAMToOCM_1 =
  over LreController
  pre "st= CAM \<and> cda\<ge>minSafeDist()"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event advVel] @ [State OCM]
         ,triggers\<Zprime> = {reqVel, reqMOM, reqHdng}
         ]"
        

  
definition Init :: "LreController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,triggers\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine LreControllerMachine =
  init Init
  invariant LreController_inv
  operations  InitialToOCM OCMToOCM OCMToOCM_1 OCMToMOM MOMToOCM MOMToOCM_1 MOMToHCM MOMToCAM MOMToOCM_2 MOMToHCM_1 MOMToHCM_2 MOMToHCM_3 HCMToOCM HCMToCAM HCMToOCM_1 HCMToMOM CAMToOCM CAMToOCM_1 


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

lemma MOMToOCM_1_inv [hoare_lemmas]: "MOMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_inv [hoare_lemmas]: "MOMToHCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToCAM_inv [hoare_lemmas]: "MOMToCAM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_2_inv [hoare_lemmas]: "MOMToOCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_1_inv [hoare_lemmas]: "MOMToHCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_2_inv [hoare_lemmas]: "MOMToHCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_3_inv [hoare_lemmas]: "MOMToHCM_3() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_inv [hoare_lemmas]: "HCMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToCAM_inv [hoare_lemmas]: "HCMToCAM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_1_inv [hoare_lemmas]: "HCMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToMOM_inv [hoare_lemmas]: "HCMToMOM() preserves LreController_inv"
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
  
lemma "MOMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToCAM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_3() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToCAM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToMOM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "CAMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "LreController_axioms = True"

lemma LreController_deadlock_free: "LreController_axioms  \<Longrightarrow> deadlock_free LreControllerMachine"
  unfolding LreControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc)
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
