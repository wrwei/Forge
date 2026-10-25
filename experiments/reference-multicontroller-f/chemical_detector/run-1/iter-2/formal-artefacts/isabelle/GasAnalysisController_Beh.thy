theory GasAnalysisController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the GasAnalysisController state machine in Z Machine notations.\<close>

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
   
enumtype St = READING | ANALYSIS | NO_GAS | GAS_DETECTED | FINAL | initial 
 


enumtype Evt = gas | resume | stop | turn 
 


enumtype Angle = Left | Right | Back | Front

enumtype Loc = left | right | front

enumtype Status = noGas | gasD

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record GasSensor = c :: int i :: real
record_default GasSensor
show_record GasSensor

definition SeqGs :: "((GasSensor) list) set" where [simp]: "SeqGs = UNIV"

text \<open> function definition \<close>

consts intensity :: " GasSensor list \<Rightarrow> real"
consts analysis :: " GasSensor list \<Rightarrow> Status"
consts location :: " GasSensor list \<Rightarrow> Angle"
consts thr :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts evasionStart :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckPeriod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore GasAnalysisController =
  gs :: "GasSensor list"
  sts :: "Status"
  ins :: "real"
  anl :: "Angle"
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
                      \<and> (st = READING \<longrightarrow> listens = {gas})
                      \<and> (st = FINAL \<longrightarrow> listens = {gas})
                                                                              \<and> (st = READING \<longrightarrow> (offered = {} \<or> offered = {gas}))
                                                                              \<and> (st = ANALYSIS \<longrightarrow> (offered = {}))
                                                                              \<and> (st = NO_GAS \<longrightarrow> (offered = {}))
                                                                              \<and> (st = GAS_DETECTED \<longrightarrow> (offered = {}))
                                                                              \<and> (st = FINAL \<longrightarrow> (offered = {} \<or> offered = {gas}))
                                                                              \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToREADING =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= initial"
  update "[st\<Zprime>= READING
         ,tr\<Zprime> =tr  @ [State READING]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation READINGToANALYSIS =
  over GasAnalysisController
  params gs_input \<in> "SeqGs" 
  pre "st= READING \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= ANALYSIS
         ,gs\<Zprime> =gs_input
         ,sts\<Zprime> = analysis(gs_input)
         ,tr\<Zprime> =tr @ [Event gas] @ [State ANALYSIS]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation ANALYSISToNO_GAS =
  over GasAnalysisController
  pre "st= ANALYSIS \<and> sts= (noGas)"
  update "[st\<Zprime>= NO_GAS
         ,tr\<Zprime> =tr @ [Event resume] @ [State NO_GAS]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation ANALYSISToGAS_DETECTED =
  over GasAnalysisController
  pre "st= ANALYSIS \<and> \<not>sts= (noGas)"
  update "[st\<Zprime>= GAS_DETECTED
         ,ins\<Zprime> = intensity(gs)
         ,tr\<Zprime> =tr  @ [State GAS_DETECTED]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NO_GASToREADING =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= NO_GAS"
  update "[st\<Zprime>= READING
         ,tr\<Zprime> =tr  @ [State READING]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GAS_DETECTEDToFINAL =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= GAS_DETECTED \<and> ins\<ge>thr()"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event stop] @ [State FINAL]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GAS_DETECTEDToREADING =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= GAS_DETECTED \<and> \<not>ins\<ge>thr()"
  update "[st\<Zprime>= READING
         ,anl\<Zprime> = location(gs)
         ,tr\<Zprime> =tr @ [Event turn] @ [State READING]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FINALToFINAL =
  over GasAnalysisController
  params gs_input \<in> "SeqGs" nxt \<in> "{{}, {gas}}" 
  pre "st= FINAL \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,gs\<Zprime> =gs_input
         ,tr\<Zprime> =tr @ [Event gas] @ [State FINAL]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_READING =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}"
  pre "st= READING \<and> \<not>(gas \<in> offered)"
  update "[st\<Zprime>= READING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_FINAL =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}"
  pre "st= FINAL \<and> \<not>(gas \<in> offered)"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "GasAnalysisController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine GasAnalysisControllerMachine =
  init Init
  invariant GasAnalysisController_inv
  operations  InitialToREADING READINGToANALYSIS ANALYSISToNO_GAS ANALYSISToGAS_DETECTED NO_GASToREADING GAS_DETECTEDToFINAL GAS_DETECTEDToREADING FINALToFINAL Idle_READING Idle_FINAL


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes GasAnalysisController_inv"
  by zpog_full

lemma InitialToREADING_inv [hoare_lemmas]: "InitialToREADING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma READINGToANALYSIS_inv [hoare_lemmas]: "READINGToANALYSIS (gs_input) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma ANALYSISToNO_GAS_inv [hoare_lemmas]: "ANALYSISToNO_GAS() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma ANALYSISToGAS_DETECTED_inv [hoare_lemmas]: "ANALYSISToGAS_DETECTED() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NO_GASToREADING_inv [hoare_lemmas]: "NO_GASToREADING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GAS_DETECTEDToFINAL_inv [hoare_lemmas]: "GAS_DETECTEDToFINAL (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GAS_DETECTEDToREADING_inv [hoare_lemmas]: "GAS_DETECTEDToREADING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FINALToFINAL_inv [hoare_lemmas]: "FINALToFINAL (gs_input, nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_READING_inv [hoare_lemmas]: "Idle_READING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_FINAL_inv [hoare_lemmas]: "Idle_FINAL (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToREADING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "READINGToANALYSIS (gs_input) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ANALYSISToNO_GAS() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ANALYSISToGAS_DETECTED() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "NO_GASToREADING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GAS_DETECTEDToFINAL (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GAS_DETECTEDToREADING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "FINALToFINAL (gs_input, nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_READING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_FINAL (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "GasAnalysisController_axioms = True"

lemma GasAnalysisController_deadlock_free: "GasAnalysisController_axioms  \<Longrightarrow> deadlock_free GasAnalysisControllerMachine"
  unfolding GasAnalysisControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
