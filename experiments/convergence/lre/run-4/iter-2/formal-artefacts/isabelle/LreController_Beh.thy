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
 


enumtype Evt = reqVel | advVel | reqHdng | advHdng | reqMOM | reqOCM | endTask | reqHCM | tick 
 


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

definition OpVel :: "real set" where [simp]: "OpVel = UNIV"
definition OpHdng :: "real set" where [simp]: "OpHdng = UNIV"

text \<open> function definition \<close>

consts sqrt :: " real \<Rightarrow> real"
consts nsRelDist :: " real \<Rightarrow> real"
consts odist :: " nat \<Rightarrow> real"
consts ewRelDist :: " real \<Rightarrow> real"
consts obsEwVel :: " real \<Rightarrow> real"
consts hdist :: " nat \<Rightarrow> real"
consts vdist :: " nat \<Rightarrow> real"
consts obsNsVel :: " real \<Rightarrow> real"
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
  opVel :: "real"
  opHdng :: "real"
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
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation OCMToOCM =
  over LreController
  params opVel_input \<in> "OpVel" 
  pre "st= OCM"
  update "[st\<Zprime>= OCM
         ,opVel\<Zprime> =opVel_input
         ,tr\<Zprime> =tr @ [Event reqVel]@ [Event advVel] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation OCMToOCM_1 =
  over LreController
  params opHdng_input \<in> "OpHdng" 
  pre "st= OCM"
  update "[st\<Zprime>= OCM
         ,opHdng\<Zprime> =opHdng_input
         ,tr\<Zprime> =tr @ [Event reqHdng]@ [Event advHdng] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation OCMToMOM =
  over LreController
  pre "st= OCM \<and> vel\<le>1.0 \<and> \<not>inOpez \<and> odist(cdyn)>minSafeDist() \<and> odist(cstc)>minSafeDist()"
  update "[st\<Zprime>= MOM
         ,tr\<Zprime> =tr @ [Event reqMOM]@ [Event advVel] @ [State MOM]
         ,triggers\<Zprime> = {reqHCM, tick, tick, endTask, reqOCM, tick, tick, tick}
         ]"
        
zoperation MOMToOCM =
  over LreController
  pre "st= MOM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation MOMToOCM_1 =
  over LreController
  pre "st= MOM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event advVel] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation MOMToHCM =
  over LreController
  pre "st= MOM"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event reqHCM]@ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {tick, tick, reqOCM, tick}
         ]"
        
zoperation MOMToOCM_2 =
  over LreController
  pre "st= MOM \<and> inOpez"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event tick] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation MOMToCAM =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> cda<minSafeDist() \<and> tcpa\<ge>0.0"
  update "[st\<Zprime>= CAM
         ,tr\<Zprime> =tr @ [Event tick] @ [State CAM]
         ,triggers\<Zprime> = {tick, reqOCM}
         ]"
        
zoperation MOMToHCM_1 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>cda<minSafeDist() \<and> tcpa\<ge>0.0 \<and> hvel\<ge>1.0 \<and> hdist(cstc)\<le>staticObsHorizDist()"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event tick]@ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {tick, tick, reqOCM, tick}
         ]"
        
zoperation MOMToHCM_2 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>cda<minSafeDist() \<and> tcpa\<ge>0.0 \<and> vdist(cstc)\<le>staticObsDfltVertDist()"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event tick]@ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {tick, tick, reqOCM, tick}
         ]"
        
zoperation MOMToHCM_3 =
  over LreController
  pre "st= MOM \<and> \<not>inOpez \<and> \<not>cda<minSafeDist() \<and> tcpa\<ge>0.0 \<and> vvel\<ge>1.0 \<and> vdist(cstc)\<le>staticObsVertDist()"
  update "[st\<Zprime>= HCM
         ,tr\<Zprime> =tr @ [Event tick]@ [Event advVel] @ [State HCM]
         ,triggers\<Zprime> = {tick, tick, reqOCM, tick}
         ]"
        
zoperation HCMToOCM =
  over LreController
  pre "st= HCM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation HCMToOCM_1 =
  over LreController
  pre "st= HCM \<and> inOpez"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event tick] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation HCMToCAM =
  over LreController
  pre "st= HCM \<and> \<not>inOpez \<and> cda<minSafeDist() \<and> tcpa\<ge>0.0"
  update "[st\<Zprime>= CAM
         ,tr\<Zprime> =tr @ [Event tick] @ [State CAM]
         ,triggers\<Zprime> = {tick, reqOCM}
         ]"
        
zoperation HCMToMOM =
  over LreController
  pre "st= HCM \<and> \<not>inOpez \<and> \<not>cda<minSafeDist() \<and> tcpa\<ge>0.0 \<and> \<not>hdist(cstc)\<le>staticObsHorizDist() \<and> \<not>vdist(cstc)\<le>staticObsVertDist()"
  update "[st\<Zprime>= MOM
         ,tr\<Zprime> =tr @ [Event tick]@ [Event advVel] @ [State MOM]
         ,triggers\<Zprime> = {reqHCM, tick, tick, endTask, reqOCM, tick, tick, tick}
         ]"
        
zoperation CAMToOCM =
  over LreController
  pre "st= CAM"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event reqOCM] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
         ]"
        
zoperation CAMToOCM_1 =
  over LreController
  pre "st= CAM \<and> \<not>cda<minSafeDist()"
  update "[st\<Zprime>= OCM
         ,tr\<Zprime> =tr @ [Event tick]@ [Event advVel] @ [State OCM]
         ,triggers\<Zprime> = {reqMOM, reqHdng, reqVel}
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
  operations  InitialToOCM OCMToOCM OCMToOCM_1 OCMToMOM MOMToOCM MOMToOCM_1 MOMToHCM MOMToOCM_2 MOMToCAM MOMToHCM_1 MOMToHCM_2 MOMToHCM_3 HCMToOCM HCMToOCM_1 HCMToCAM HCMToMOM CAMToOCM CAMToOCM_1 


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes LreController_inv"
  by zpog_full

lemma InitialToOCM_inv [hoare_lemmas]: "InitialToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma OCMToOCM_inv [hoare_lemmas]: "OCMToOCM (opVel_input) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToOCM_1_inv [hoare_lemmas]: "OCMToOCM_1 (opHdng_input) preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma OCMToMOM_inv [hoare_lemmas]: "OCMToMOM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_inv [hoare_lemmas]: "MOMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_1_inv [hoare_lemmas]: "MOMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_inv [hoare_lemmas]: "MOMToHCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToOCM_2_inv [hoare_lemmas]: "MOMToOCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToCAM_inv [hoare_lemmas]: "MOMToCAM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_1_inv [hoare_lemmas]: "MOMToHCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_2_inv [hoare_lemmas]: "MOMToHCM_2() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOMToHCM_3_inv [hoare_lemmas]: "MOMToHCM_3() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_inv [hoare_lemmas]: "HCMToOCM() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToOCM_1_inv [hoare_lemmas]: "HCMToOCM_1() preserves LreController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HCMToCAM_inv [hoare_lemmas]: "HCMToCAM() preserves LreController_inv"
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
  
lemma "OCMToOCM (opVel_input) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToOCM_1 (opHdng_input) preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "OCMToMOM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToOCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToCAM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_2() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "MOMToHCM_3() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToOCM_1() preserves R1 under LreController_inv"
  by (zpog_full; auto)
  
lemma "HCMToCAM() preserves R1 under LreController_inv"
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
  (* FORK: the deadlock_free closing tactic is selected by `hasSeqPayloadDomain`
     (set in PART 2 when this machine emits a SEQ-typed payload domain set such
     as `SeqGs`). The residual goal after `apply deadlock_free` is
       \<And>st. st = M\<^sub>1 \<or> ... \<or> guarded-disjuncts
     and St.exhaust_disc is the enumtype discriminator exhaustion lemma that
     matches the bare `st = X` disjunct for each state. Two cases:
       * has SEQ payload domain -> `using St.exhaust_disc by auto`. metis HANGS
         (>10 min) when a state's ONLY transition consumes a Seq-typed payload
         (e.g. gas-analysis Reading: `params gs_input \<in> SeqGs`), because the
         enabledness disjunct is `\<exists>gs_input \<in> SeqGs. ...`. With the set
         emitted as a [simp] `= UNIV` definition (PART 2), `auto` discharges the
         existential and closes via the bare disjunct. Verified on
         chemical_detector (gas-analysis, ~15s).
       * otherwise (no payload domain, or only SCALAR TypeRef/ProductType
         payload domains) -> `by (metis St.exhaust_disc)` (the ICECCS2023 LRE
         tactic). Scalar payload sets are [simp] `= UNIV` too, so the
         deadlock_free method's internal simp discharges their existentials and
         the residual is param-free — metis closes it. `auto` TIMES OUT
         (>600s) on real-arithmetic guard disjuncts (confirmed twice: classic
         LRE regression, and 2026-06 LRE run-7 where OpVel/OpHdng scalar
         payload domains + real-arith guards made the old hasPayloadDomain
         selection pick auto -> per-goal timeout; metis closed the same
         residual in ~88s).
     Tried and rejected: `(cases st; simp_all)` / `(cases st; auto)` leave the
     guarded disjunct (e.g. `thr() \<le> ins`) open because the split selects the
     guarded disjunct instead of the bare one; `apply simp` after
     `apply deadlock_free` fails with no-progress (the residual is already
     simp-normal). *)
end
