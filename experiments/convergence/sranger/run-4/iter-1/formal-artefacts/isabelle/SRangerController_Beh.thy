theory SRangerController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the SRangerController state machine in Z Machine notations.\<close>

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
   
enumtype St = MOVING | TURNING | HALTED | initial 
 


enumtype Evt = endTask | obstacle | tick 
 


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

consts distance :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts obstacleThreshold :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts clockResetTime :: "unit \<Rightarrow> nat"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts turnDuration :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SRangerController =
  st::"St"
  tr :: "(St, Evt) tag list"
  triggers:: "Evt set"
  where inv:
    "tr \<noteq> []"

subsection \<open> Operations \<close>

zoperation InitialToMOVING =
  over SRangerController
  pre "st= initial"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr  @ [State MOVING]
         ,triggers\<Zprime> = {endTask, tick, obstacle}
         ]"
        
zoperation MOVINGToHALTED =
  over SRangerController
  pre "st= MOVING"
  update "[st\<Zprime>= HALTED
         ,tr\<Zprime> =tr @ [Event endTask] @ [State HALTED]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation MOVINGToTURNING =
  over SRangerController
  pre "st= MOVING \<and> distance()\<le>obstacleThreshold()"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State TURNING]
         ,triggers\<Zprime> = {endTask, tick}
         ]"
        
zoperation MOVINGToMOVING =
  over SRangerController
  pre "st= MOVING"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr @ [Event tick] @ [State MOVING]
         ,triggers\<Zprime> = {endTask, tick, obstacle}
         ]"
        
zoperation TURNINGToHALTED =
  over SRangerController
  pre "st= TURNING"
  update "[st\<Zprime>= HALTED
         ,tr\<Zprime> =tr @ [Event endTask] @ [State HALTED]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation TURNINGToMOVING =
  over SRangerController
  pre "st= TURNING \<and> since(clockResetTime())\<ge>turnDuration()"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr  @ [State MOVING]
         ,triggers\<Zprime> = {endTask, tick, obstacle}
         ]"
        
zoperation TURNINGToTURNING =
  over SRangerController
  pre "st= TURNING"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event tick] @ [State TURNING]
         ,triggers\<Zprime> = {endTask, tick}
         ]"
        
zoperation HALTEDToHALTED =
  over SRangerController
  pre "st= HALTED"
  update "[st\<Zprime>= HALTED
         ,tr\<Zprime> =tr @ [Event tick] @ [State HALTED]
         ,triggers\<Zprime> = {tick}
         ]"
        

  
definition Init :: "SRangerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,triggers\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SRangerControllerMachine =
  init Init
  invariant SRangerController_inv
  operations  InitialToMOVING MOVINGToHALTED MOVINGToTURNING MOVINGToMOVING TURNINGToHALTED TURNINGToMOVING TURNINGToTURNING HALTEDToHALTED 


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMOVING_inv [hoare_lemmas]: "InitialToMOVING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToHALTED_inv [hoare_lemmas]: "MOVINGToHALTED() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToTURNING_inv [hoare_lemmas]: "MOVINGToTURNING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToMOVING_inv [hoare_lemmas]: "MOVINGToMOVING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToHALTED_inv [hoare_lemmas]: "TURNINGToHALTED() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToMOVING_inv [hoare_lemmas]: "TURNINGToMOVING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToTURNING_inv [hoare_lemmas]: "TURNINGToTURNING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HALTEDToHALTED_inv [hoare_lemmas]: "HALTEDToHALTED() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMOVING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToHALTED() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToTURNING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToMOVING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToHALTED() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToMOVING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToTURNING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "HALTEDToHALTED() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
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
