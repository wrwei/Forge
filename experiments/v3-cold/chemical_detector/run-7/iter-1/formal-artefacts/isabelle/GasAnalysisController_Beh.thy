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
   
enumtype St = READING | ANALYSING | NO_GAS | GAS_DETECTED | CONCLUDED | initial 
 


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
record Chem = id :: int
record_default Chem
show_record Chem

record GasMeasurement = c :: Chem i :: real
record_default GasMeasurement
show_record GasMeasurement

definition SeqGs :: "((GasMeasurement) list) set" where [simp]: "SeqGs = UNIV"

text \<open> function definition \<close>

consts intensity :: " GasMeasurement list \<Rightarrow> real"
consts analysis :: " GasMeasurement list \<Rightarrow> Status"
consts location :: " GasMeasurement list \<Rightarrow> Angle"
consts peakAtOrAboveThreshold :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts evasionStart :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckPeriod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore GasAnalysisController =
  gs :: "GasMeasurement list"
  sts :: "Status"
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
                                                                                \<and> (st = READING \<longrightarrow> (offered = {} \<or> offered = {gas}))
                                                                                \<and> (st = ANALYSING \<longrightarrow> (offered = {}))
                                                                                \<and> (st = NO_GAS \<longrightarrow> (offered = {}))
                                                                                \<and> (st = GAS_DETECTED \<longrightarrow> (offered = {}))
                                                                                \<and> (st = CONCLUDED \<longrightarrow> (offered = {}))
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
        
zoperation READINGToANALYSING =
  over GasAnalysisController
  params gs_input \<in> "SeqGs" 
  pre "st= READING \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= ANALYSING
         ,gs\<Zprime> =gs_input
         ,sts\<Zprime> = analysis(gs_input)
         ,tr\<Zprime> =tr @ [Event gas] @ [State ANALYSING]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation ANALYSINGToGAS_DETECTED =
  over GasAnalysisController
  pre "st= ANALYSING \<and> sts= (gasD)"
  update "[st\<Zprime>= GAS_DETECTED
         ,tr\<Zprime> =tr  @ [State GAS_DETECTED]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation ANALYSINGToNO_GAS =
  over GasAnalysisController
  pre "st= ANALYSING \<and> \<not>sts= (gasD)"
  update "[st\<Zprime>= NO_GAS
         ,tr\<Zprime> =tr @ [Event resume] @ [State NO_GAS]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NO_GASToREADING =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= NO_GAS \<and> True"
  update "[st\<Zprime>= READING
         ,tr\<Zprime> =tr  @ [State READING]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GAS_DETECTEDToCONCLUDED =
  over GasAnalysisController
  pre "st= GAS_DETECTED \<and> peakAtOrAboveThreshold()"
  update "[st\<Zprime>= CONCLUDED
         ,tr\<Zprime> =tr @ [Event stop] @ [State CONCLUDED]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GAS_DETECTEDToREADING =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= GAS_DETECTED \<and> \<not>peakAtOrAboveThreshold()"
  update "[st\<Zprime>= READING
         ,anl\<Zprime> = location(gs)
         ,tr\<Zprime> =tr @ [Event turn] @ [State READING]
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

zoperation Idle_NO_GAS =
  over GasAnalysisController
  pre "st= NO_GAS \<and> \<not>(True)"
  update "[st\<Zprime>= NO_GAS
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = {}
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
  operations  InitialToREADING READINGToANALYSING ANALYSINGToGAS_DETECTED ANALYSINGToNO_GAS NO_GASToREADING GAS_DETECTEDToCONCLUDED GAS_DETECTEDToREADING Idle_READING Idle_NO_GAS
  until "st = CONCLUDED"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes GasAnalysisController_inv"
  by zpog_full

lemma InitialToREADING_inv [hoare_lemmas]: "InitialToREADING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma READINGToANALYSING_inv [hoare_lemmas]: "READINGToANALYSING (gs_input) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma ANALYSINGToGAS_DETECTED_inv [hoare_lemmas]: "ANALYSINGToGAS_DETECTED() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma ANALYSINGToNO_GAS_inv [hoare_lemmas]: "ANALYSINGToNO_GAS() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NO_GASToREADING_inv [hoare_lemmas]: "NO_GASToREADING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GAS_DETECTEDToCONCLUDED_inv [hoare_lemmas]: "GAS_DETECTEDToCONCLUDED() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GAS_DETECTEDToREADING_inv [hoare_lemmas]: "GAS_DETECTEDToREADING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_READING_inv [hoare_lemmas]: "Idle_READING (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_NO_GAS_inv [hoare_lemmas]: "Idle_NO_GAS() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToREADING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "READINGToANALYSING (gs_input) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ANALYSINGToGAS_DETECTED() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ANALYSINGToNO_GAS() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "NO_GASToREADING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GAS_DETECTEDToCONCLUDED() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GAS_DETECTEDToREADING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_READING (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_NO_GAS() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "GasAnalysisController_axioms = True"

(* Terminal state(s), with no outgoing transition: CONCLUDED.
   The machine's `until` clause exempts them from the deadlock-freedom
   obligation below: the machine has finished there. *)
lemma GasAnalysisController_deadlock_free: "GasAnalysisController_axioms  \<Longrightarrow> deadlock_free GasAnalysisControllerMachine"
  unfolding GasAnalysisControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
