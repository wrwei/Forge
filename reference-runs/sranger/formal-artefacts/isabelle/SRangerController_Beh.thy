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
   
enumtype St = Moving | Turning | Final | initial 
 


enumtype Evt = endTask | move | obstacle | markReset | tick 
 


type_synonym move_Type_lv_double_av_double= "nat"
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
consts obstaclethreshold :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SRangerController =
  turnDurationElapsed :: "bool"
  st::"St"
  tr :: "(St, Evt) tag list"
  triggers:: "Evt set"
  where inv:
    "tr \<noteq> []"

subsection \<open> Operations \<close>

zoperation InitialToMoving =
  over SRangerController
  pre "st= initial"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> =tr  @ [State Moving]
         ,triggers\<Zprime> = {obstacle, tick, endTask}
         ]"
        
zoperation MovingToFinal =
  over SRangerController
  pre "st= Moving"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation MovingToTurning =
  over SRangerController
  pre "st= Moving \<and> distance()\<le>obstaclethreshold()"
  update "[st\<Zprime>= Turning
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event markReset]@ [Event move] @ [State Turning]
         ,triggers\<Zprime> = {tick, endTask}
         ]"
        
zoperation MovingToMoving =
  over SRangerController
  pre "st= Moving"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> =tr @ [Event tick] @ [State Moving]
         ,triggers\<Zprime> = {obstacle, tick, endTask}
         ]"
        
zoperation TurningToMoving =
  over SRangerController
  pre "st= Turning \<and> turnDurationElapsed"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> =tr @ [Event move] @ [State Moving]
         ,triggers\<Zprime> = {obstacle, tick, endTask}
         ]"
        
zoperation TurningToFinal =
  over SRangerController
  pre "st= Turning"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation TurningToTurning =
  over SRangerController
  pre "st= Turning"
  update "[st\<Zprime>= Turning
         ,tr\<Zprime> =tr @ [Event tick] @ [State Turning]
         ,triggers\<Zprime> = {tick, endTask}
         ]"
        
zoperation FinalToFinal =
  over SRangerController
  pre "st= Final"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
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
  operations  InitialToMoving MovingToFinal MovingToTurning MovingToMoving TurningToMoving TurningToFinal TurningToTurning FinalToFinal 


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMoving_inv [hoare_lemmas]: "InitialToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MovingToFinal_inv [hoare_lemmas]: "MovingToFinal() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MovingToTurning_inv [hoare_lemmas]: "MovingToTurning() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MovingToMoving_inv [hoare_lemmas]: "MovingToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToMoving_inv [hoare_lemmas]: "TurningToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToFinal_inv [hoare_lemmas]: "TurningToFinal() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToTurning_inv [hoare_lemmas]: "TurningToTurning() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToFinal() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToTurning() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToFinal() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToTurning() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc)
  (* FORK: archive closes the residual disjunctive goal
       \<And>st. st = M\<^sub>1 \<or> ... \<or> guarded-disjuncts
     via `by (metis St.exhaust_disc)` (see ICECCS2023 archive
     LRE_Z_Machine_deadlock_free.thy). St.exhaust_disc is the
     enumtype discriminator exhaustion lemma generated for `enumtype St`;
     metis instantiates it for the current `st` and matches the plain
     `st = X` disjunct to True, closing every case.
     Tried and rejected: `apply (cases st; simp_all)` fails on the mixed
     bare/guarded disjunction; `by (deadlock_free; cases st; auto)` times
     out (>10 min) because `;` makes \<And>st inaccessible to cases. *)
end
