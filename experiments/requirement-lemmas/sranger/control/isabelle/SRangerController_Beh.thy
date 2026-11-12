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
definition obstaclethreshold :: "unit \<Rightarrow> real" where "obstaclethreshold u = 0.5"  (* named constant; value from the model *)

subsection \<open> State Space \<close>

zstore SRangerController =
  turnDurationElapsed :: "bool"
  resetTime :: "real"
  elapsed :: "bool"
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
                      \<and> (st = Moving \<longrightarrow> listens = {tick, obstacle, endTask})
                      \<and> (st = Turning \<longrightarrow> listens = {endTask, tick})
                      \<and> (st = Final \<longrightarrow> listens = {tick})
                                                                               \<and> (st = Moving \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {obstacle} \<or> offered = {endTask}))
                                                                               \<and> (st = Turning \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {tick}))
                                                                               \<and> (st = Final \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToMoving =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}" 
  pre "st= initial"
  update "[st\<Zprime>= Moving
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr  @ [State Moving]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MovingToFinal =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Moving \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MovingToTurning =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Moving \<and> distance()\<le>obstaclethreshold() \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MovingToMoving =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}" 
  pre "st= Moving \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Moving
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event tick] @ [State Moving]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TurningToMoving =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}" 
  pre "st= Turning \<and> turnDurationElapsed"
  update "[st\<Zprime>= Moving
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event move] @ [State Moving]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TurningToFinal =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Turning \<and> \<not>turnDurationElapsed \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TurningToTurning =
  over SRangerController
  params nxt \<in> "{{}, {endTask}, {tick}}" 
  pre "st= Turning \<and> \<not>turnDurationElapsed \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Turning
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event tick] @ [State Turning]
         ,listens\<Zprime> = {endTask, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FinalToFinal =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Final \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_Moving =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}"
  pre "st= Moving \<and> \<not>(endTask \<in> offered) \<and> \<not>(distance()\<le>obstaclethreshold() \<and> obstacle \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Turning =
  over SRangerController
  params nxt \<in> "{{}, {endTask}, {tick}}"
  pre "st= Turning \<and> \<not>(turnDurationElapsed) \<and> \<not>(endTask \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Turning
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Final =
  over SRangerController
  params nxt \<in> "{{}, {tick}}"
  pre "st= Final \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "SRangerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SRangerControllerMachine =
  init Init
  invariant SRangerController_inv
  operations  InitialToMoving MovingToFinal MovingToTurning MovingToMoving TurningToMoving TurningToFinal TurningToTurning FinalToFinal Idle_Moving Idle_Turning Idle_Final


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMoving_inv [hoare_lemmas]: "InitialToMoving (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MovingToFinal_inv [hoare_lemmas]: "MovingToFinal (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MovingToTurning_inv [hoare_lemmas]: "MovingToTurning (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MovingToMoving_inv [hoare_lemmas]: "MovingToMoving (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TurningToMoving_inv [hoare_lemmas]: "TurningToMoving (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TurningToFinal_inv [hoare_lemmas]: "TurningToFinal (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TurningToTurning_inv [hoare_lemmas]: "TurningToTurning (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Moving_inv [hoare_lemmas]: "Idle_Moving (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Turning_inv [hoare_lemmas]: "Idle_Turning (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Final_inv [hoare_lemmas]: "Idle_Final (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMoving (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToFinal (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToTurning (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToMoving (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToMoving (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToFinal (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToTurning (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Moving (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Turning (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Final (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+

subsection \<open> Requirement-derived obligations (SR-Beh1..SR-Beh7) \<close>

zexpr Pre_Init is "st = initial \<and> offered = {}"
zexpr Adm_Init is "st \<in> {Moving}"
lemma Req_Init_InitialToMoving: "H{Pre_Init \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MovingToFinal: "H{Pre_Init \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MovingToTurning: "H{Pre_Init \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_MovingToMoving: "H{Pre_Init \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_TurningToMoving: "H{Pre_Init \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_TurningToFinal: "H{Pre_Init \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_TurningToTurning: "H{Pre_Init \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_FinalToFinal: "H{Pre_Init \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_Moving: "H{Pre_Init \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_Turning: "H{Pre_Init \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Init}"
  by (zpog_full; simp)
lemma Req_Init_Idle_Final: "H{Pre_Init \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Init}"
  by (zpog_full; simp)

zexpr Pre_Moving_none_O is "st = Moving \<and> offered = {} \<and> (distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_none_O is "st \<in> {Moving}"
lemma Req_Moving_none_O_InitialToMoving: "H{Pre_Moving_none_O \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_MovingToFinal: "H{Pre_Moving_none_O \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_MovingToTurning: "H{Pre_Moving_none_O \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_MovingToMoving: "H{Pre_Moving_none_O \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_TurningToMoving: "H{Pre_Moving_none_O \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_TurningToFinal: "H{Pre_Moving_none_O \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_TurningToTurning: "H{Pre_Moving_none_O \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_FinalToFinal: "H{Pre_Moving_none_O \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_Idle_Moving: "H{Pre_Moving_none_O \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_Idle_Turning: "H{Pre_Moving_none_O \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)
lemma Req_Moving_none_O_Idle_Final: "H{Pre_Moving_none_O \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_none_O}"
  by (zpog_full; simp)

zexpr Pre_Moving_none_o is "st = Moving \<and> offered = {} \<and> \<not>(distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_none_o is "st \<in> {Moving}"
lemma Req_Moving_none_o_InitialToMoving: "H{Pre_Moving_none_o \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_MovingToFinal: "H{Pre_Moving_none_o \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_MovingToTurning: "H{Pre_Moving_none_o \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_MovingToMoving: "H{Pre_Moving_none_o \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_TurningToMoving: "H{Pre_Moving_none_o \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_TurningToFinal: "H{Pre_Moving_none_o \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_TurningToTurning: "H{Pre_Moving_none_o \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_FinalToFinal: "H{Pre_Moving_none_o \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_Idle_Moving: "H{Pre_Moving_none_o \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_Idle_Turning: "H{Pre_Moving_none_o \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)
lemma Req_Moving_none_o_Idle_Final: "H{Pre_Moving_none_o \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_none_o}"
  by (zpog_full; simp)

zexpr Pre_Moving_endTask_O is "st = Moving \<and> offered = {endTask} \<and> (distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_endTask_O is "st \<in> {Final}"
lemma Req_Moving_endTask_O_InitialToMoving: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_MovingToFinal: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_MovingToTurning: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_MovingToMoving: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_TurningToMoving: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_TurningToFinal: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_TurningToTurning: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_FinalToFinal: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_Idle_Moving: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_Idle_Turning: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_O_Idle_Final: "H{Pre_Moving_endTask_O \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_endTask_O}"
  by (zpog_full; simp)

zexpr Pre_Moving_endTask_o is "st = Moving \<and> offered = {endTask} \<and> \<not>(distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_endTask_o is "st \<in> {Final}"
lemma Req_Moving_endTask_o_InitialToMoving: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_MovingToFinal: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_MovingToTurning: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_MovingToMoving: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_TurningToMoving: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_TurningToFinal: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_TurningToTurning: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_FinalToFinal: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_Idle_Moving: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_Idle_Turning: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)
lemma Req_Moving_endTask_o_Idle_Final: "H{Pre_Moving_endTask_o \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_endTask_o}"
  by (zpog_full; simp)

zexpr Pre_Moving_obstacle_O is "st = Moving \<and> offered = {obstacle} \<and> (distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_obstacle_O is "st \<in> {Turning}"
lemma Req_Moving_obstacle_O_InitialToMoving: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_MovingToFinal: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_MovingToTurning: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_MovingToMoving: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_TurningToMoving: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_TurningToFinal: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_TurningToTurning: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_FinalToFinal: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_Idle_Moving: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_Idle_Turning: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_O_Idle_Final: "H{Pre_Moving_obstacle_O \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_obstacle_O}"
  by (zpog_full; simp)

zexpr Pre_Moving_obstacle_o is "st = Moving \<and> offered = {obstacle} \<and> \<not>(distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_obstacle_o is "st \<in> {Moving}"
lemma Req_Moving_obstacle_o_InitialToMoving: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_MovingToFinal: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_MovingToTurning: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_MovingToMoving: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_TurningToMoving: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_TurningToFinal: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_TurningToTurning: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_FinalToFinal: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_Idle_Moving: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_Idle_Turning: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)
lemma Req_Moving_obstacle_o_Idle_Final: "H{Pre_Moving_obstacle_o \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_obstacle_o}"
  by (zpog_full; simp)

zexpr Pre_Moving_tick_O is "st = Moving \<and> offered = {tick} \<and> (distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_tick_O is "st \<in> {Moving}"
lemma Req_Moving_tick_O_InitialToMoving: "H{Pre_Moving_tick_O \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_MovingToFinal: "H{Pre_Moving_tick_O \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_MovingToTurning: "H{Pre_Moving_tick_O \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_MovingToMoving: "H{Pre_Moving_tick_O \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_TurningToMoving: "H{Pre_Moving_tick_O \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_TurningToFinal: "H{Pre_Moving_tick_O \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_TurningToTurning: "H{Pre_Moving_tick_O \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_FinalToFinal: "H{Pre_Moving_tick_O \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_Idle_Moving: "H{Pre_Moving_tick_O \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_Idle_Turning: "H{Pre_Moving_tick_O \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)
lemma Req_Moving_tick_O_Idle_Final: "H{Pre_Moving_tick_O \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_tick_O}"
  by (zpog_full; simp)

zexpr Pre_Moving_tick_o is "st = Moving \<and> offered = {tick} \<and> \<not>(distance()\<le>obstaclethreshold())"
zexpr Adm_Moving_tick_o is "st \<in> {Moving}"
lemma Req_Moving_tick_o_InitialToMoving: "H{Pre_Moving_tick_o \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_MovingToFinal: "H{Pre_Moving_tick_o \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_MovingToTurning: "H{Pre_Moving_tick_o \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_MovingToMoving: "H{Pre_Moving_tick_o \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_TurningToMoving: "H{Pre_Moving_tick_o \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_TurningToFinal: "H{Pre_Moving_tick_o \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_TurningToTurning: "H{Pre_Moving_tick_o \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_FinalToFinal: "H{Pre_Moving_tick_o \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_Idle_Moving: "H{Pre_Moving_tick_o \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_Idle_Turning: "H{Pre_Moving_tick_o \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)
lemma Req_Moving_tick_o_Idle_Final: "H{Pre_Moving_tick_o \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Moving_tick_o}"
  by (zpog_full; simp)

zexpr Pre_Turning_none_D is "st = Turning \<and> offered = {} \<and> turnDurationElapsed"
zexpr Adm_Turning_none_D is "st \<in> {Moving}"
lemma Req_Turning_none_D_InitialToMoving: "H{Pre_Turning_none_D \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_MovingToFinal: "H{Pre_Turning_none_D \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_MovingToTurning: "H{Pre_Turning_none_D \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_MovingToMoving: "H{Pre_Turning_none_D \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_TurningToMoving: "H{Pre_Turning_none_D \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_TurningToFinal: "H{Pre_Turning_none_D \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_TurningToTurning: "H{Pre_Turning_none_D \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_FinalToFinal: "H{Pre_Turning_none_D \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_Idle_Moving: "H{Pre_Turning_none_D \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_Idle_Turning: "H{Pre_Turning_none_D \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)
lemma Req_Turning_none_D_Idle_Final: "H{Pre_Turning_none_D \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Turning_none_D}"
  by (zpog_full; simp)

zexpr Pre_Turning_none_d is "st = Turning \<and> offered = {} \<and> \<not>turnDurationElapsed"
zexpr Adm_Turning_none_d is "st \<in> {Turning}"
lemma Req_Turning_none_d_InitialToMoving: "H{Pre_Turning_none_d \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_MovingToFinal: "H{Pre_Turning_none_d \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_MovingToTurning: "H{Pre_Turning_none_d \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_MovingToMoving: "H{Pre_Turning_none_d \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_TurningToMoving: "H{Pre_Turning_none_d \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_TurningToFinal: "H{Pre_Turning_none_d \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_TurningToTurning: "H{Pre_Turning_none_d \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_FinalToFinal: "H{Pre_Turning_none_d \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_Idle_Moving: "H{Pre_Turning_none_d \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_Idle_Turning: "H{Pre_Turning_none_d \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)
lemma Req_Turning_none_d_Idle_Final: "H{Pre_Turning_none_d \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Turning_none_d}"
  by (zpog_full; simp)

zexpr Pre_Turning_endTask_D is "st = Turning \<and> offered = {endTask} \<and> turnDurationElapsed"
zexpr Adm_Turning_endTask_D is "st \<in> {Final, Moving}"
lemma Req_Turning_endTask_D_InitialToMoving: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_MovingToFinal: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_MovingToTurning: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_MovingToMoving: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_TurningToMoving: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_TurningToFinal: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_TurningToTurning: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_FinalToFinal: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_Idle_Moving: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_Idle_Turning: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_D_Idle_Final: "H{Pre_Turning_endTask_D \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Turning_endTask_D}"
  by (zpog_full; simp)

zexpr Pre_Turning_endTask_d is "st = Turning \<and> offered = {endTask} \<and> \<not>turnDurationElapsed"
zexpr Adm_Turning_endTask_d is "st \<in> {Final}"
lemma Req_Turning_endTask_d_InitialToMoving: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_MovingToFinal: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_MovingToTurning: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_MovingToMoving: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_TurningToMoving: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_TurningToFinal: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_TurningToTurning: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_FinalToFinal: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_Idle_Moving: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_Idle_Turning: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)
lemma Req_Turning_endTask_d_Idle_Final: "H{Pre_Turning_endTask_d \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Turning_endTask_d}"
  by (zpog_full; simp)

zexpr Pre_Turning_tick_D is "st = Turning \<and> offered = {tick} \<and> turnDurationElapsed"
zexpr Adm_Turning_tick_D is "st \<in> {Moving, Turning}"
lemma Req_Turning_tick_D_InitialToMoving: "H{Pre_Turning_tick_D \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_MovingToFinal: "H{Pre_Turning_tick_D \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_MovingToTurning: "H{Pre_Turning_tick_D \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_MovingToMoving: "H{Pre_Turning_tick_D \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_TurningToMoving: "H{Pre_Turning_tick_D \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_TurningToFinal: "H{Pre_Turning_tick_D \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_TurningToTurning: "H{Pre_Turning_tick_D \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_FinalToFinal: "H{Pre_Turning_tick_D \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_Idle_Moving: "H{Pre_Turning_tick_D \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_Idle_Turning: "H{Pre_Turning_tick_D \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)
lemma Req_Turning_tick_D_Idle_Final: "H{Pre_Turning_tick_D \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Turning_tick_D}"
  by (zpog_full; simp)

zexpr Pre_Turning_tick_d is "st = Turning \<and> offered = {tick} \<and> \<not>turnDurationElapsed"
zexpr Adm_Turning_tick_d is "st \<in> {Turning}"
lemma Req_Turning_tick_d_InitialToMoving: "H{Pre_Turning_tick_d \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_MovingToFinal: "H{Pre_Turning_tick_d \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_MovingToTurning: "H{Pre_Turning_tick_d \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_MovingToMoving: "H{Pre_Turning_tick_d \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_TurningToMoving: "H{Pre_Turning_tick_d \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_TurningToFinal: "H{Pre_Turning_tick_d \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_TurningToTurning: "H{Pre_Turning_tick_d \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_FinalToFinal: "H{Pre_Turning_tick_d \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_Idle_Moving: "H{Pre_Turning_tick_d \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_Idle_Turning: "H{Pre_Turning_tick_d \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)
lemma Req_Turning_tick_d_Idle_Final: "H{Pre_Turning_tick_d \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Turning_tick_d}"
  by (zpog_full; simp)

zexpr Pre_Final_none_x is "st = Final \<and> offered = {}"
zexpr Adm_Final_none_x is "st \<in> {Final}"
lemma Req_Final_none_x_InitialToMoving: "H{Pre_Final_none_x \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_MovingToFinal: "H{Pre_Final_none_x \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_MovingToTurning: "H{Pre_Final_none_x \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_MovingToMoving: "H{Pre_Final_none_x \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_TurningToMoving: "H{Pre_Final_none_x \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_TurningToFinal: "H{Pre_Final_none_x \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_TurningToTurning: "H{Pre_Final_none_x \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_FinalToFinal: "H{Pre_Final_none_x \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_Idle_Moving: "H{Pre_Final_none_x \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_Idle_Turning: "H{Pre_Final_none_x \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)
lemma Req_Final_none_x_Idle_Final: "H{Pre_Final_none_x \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Final_none_x}"
  by (zpog_full; simp)

zexpr Pre_Final_tick_x is "st = Final \<and> offered = {tick}"
zexpr Adm_Final_tick_x is "st \<in> {Final}"
lemma Req_Final_tick_x_InitialToMoving: "H{Pre_Final_tick_x \<and> SRangerController_inv} InitialToMoving (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_MovingToFinal: "H{Pre_Final_tick_x \<and> SRangerController_inv} MovingToFinal (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_MovingToTurning: "H{Pre_Final_tick_x \<and> SRangerController_inv} MovingToTurning (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_MovingToMoving: "H{Pre_Final_tick_x \<and> SRangerController_inv} MovingToMoving (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_TurningToMoving: "H{Pre_Final_tick_x \<and> SRangerController_inv} TurningToMoving (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_TurningToFinal: "H{Pre_Final_tick_x \<and> SRangerController_inv} TurningToFinal (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_TurningToTurning: "H{Pre_Final_tick_x \<and> SRangerController_inv} TurningToTurning (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_FinalToFinal: "H{Pre_Final_tick_x \<and> SRangerController_inv} FinalToFinal (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_Idle_Moving: "H{Pre_Final_tick_x \<and> SRangerController_inv} Idle_Moving (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_Idle_Turning: "H{Pre_Final_tick_x \<and> SRangerController_inv} Idle_Turning (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)
lemma Req_Final_tick_x_Idle_Final: "H{Pre_Final_tick_x \<and> SRangerController_inv} Idle_Final (nxt) {_. Adm_Final_tick_x}"
  by (zpog_full; simp)

end
