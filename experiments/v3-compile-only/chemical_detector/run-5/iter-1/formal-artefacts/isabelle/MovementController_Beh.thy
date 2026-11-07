theory MovementController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the MovementController state machine in Z Machine notations.\<close>

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
   
enumtype St = WAITING | GOING | FOUND | AVOIDING | TRYING_AGAIN | AVOIDING_AGAIN | GETTING_OUT | initial 
 


enumtype Evt = stop | flag | turn | resume | randomWalk | obstacle | changeDirection | shortRandomWalk 
 


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

record GasSample = c :: Chem i :: real
record_default GasSample
show_record GasSample

definition A :: "Angle set" where [simp]: "A = UNIV"

definition L :: "Loc set" where [simp]: "L = UNIV"





text \<open> function definition \<close>

consts analysis :: " GasSample list \<Rightarrow> Status"
consts intensity :: " GasSample list \<Rightarrow> real"
consts location :: " GasSample list \<Rightarrow> Angle"
consts peakAtThreshold :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts evasionStart :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckPeriod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts odometer :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore MovementController =
  a :: "Angle"
  l :: "Loc"
  d0 :: "real"
  d1 :: "real"
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
                      \<and> (st = WAITING \<longrightarrow> listens = {turn, stop, resume})
                      \<and> (st = GOING \<longrightarrow> listens = {resume, stop, turn, obstacle})
                      \<and> (st = AVOIDING \<longrightarrow> listens = {resume, stop, turn})
                      \<and> (st = TRYING_AGAIN \<longrightarrow> listens = {resume, obstacle, turn, stop})
                      \<and> (st = AVOIDING_AGAIN \<longrightarrow> listens = {resume, stop})
                      \<and> (st = GETTING_OUT \<longrightarrow> listens = {resume, stop, turn})
                                                                                                   \<and> (st = WAITING \<longrightarrow> (offered = {} \<or> offered = {turn} \<or> offered = {stop} \<or> offered = {resume}))
                                                                                                   \<and> (st = GOING \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop} \<or> offered = {turn} \<or> offered = {obstacle}))
                                                                                                   \<and> (st = FOUND \<longrightarrow> (offered = {}))
                                                                                                   \<and> (st = AVOIDING \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop} \<or> offered = {turn}))
                                                                                                   \<and> (st = TRYING_AGAIN \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {obstacle} \<or> offered = {turn} \<or> offered = {stop}))
                                                                                                   \<and> (st = AVOIDING_AGAIN \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop}))
                                                                                                   \<and> (st = GETTING_OUT \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop} \<or> offered = {turn}))
                                                                                                   \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= initial"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr  @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WAITINGToFOUND =
  over MovementController
  pre "st= WAITING \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FOUND
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State FOUND]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation WAITINGToGOING =
  over MovementController
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {stop}, {turn}, {obstacle}}" 
  pre "st= WAITING \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GOING
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State GOING]
         ,listens\<Zprime> = {resume, stop, turn, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WAITINGToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= WAITING \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GOINGToFOUND =
  over MovementController
  pre "st= GOING \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FOUND
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State FOUND]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GOINGToGOING =
  over MovementController
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {stop}, {turn}, {obstacle}}" 
  pre "st= GOING \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GOING
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State GOING]
         ,listens\<Zprime> = {resume, stop, turn, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GOINGToAVOIDING =
  over MovementController
  params l_input \<in> "L" nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= GOING \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AVOIDING
         ,l\<Zprime> =l_input
         ,d0\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event changeDirection] @ [State AVOIDING]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GOINGToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= GOING \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AVOIDINGToFOUND =
  over MovementController
  pre "st= AVOIDING \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FOUND
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State FOUND]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AVOIDINGToTRYING_AGAIN =
  over MovementController
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}" 
  pre "st= AVOIDING \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TRYING_AGAIN
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TRYING_AGAIN]
         ,listens\<Zprime> = {resume, obstacle, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AVOIDINGToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= AVOIDING \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TRYING_AGAINToFOUND =
  over MovementController
  pre "st= TRYING_AGAIN \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FOUND
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State FOUND]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation TRYING_AGAINToTRYING_AGAIN =
  over MovementController
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}" 
  pre "st= TRYING_AGAIN \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TRYING_AGAIN
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TRYING_AGAIN]
         ,listens\<Zprime> = {resume, obstacle, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TRYING_AGAINToAVOIDING_AGAIN =
  over MovementController
  params l_input \<in> "L" nxt \<in> "{{}, {resume}, {stop}}" 
  pre "st= TRYING_AGAIN \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AVOIDING_AGAIN
         ,l\<Zprime> =l_input
         ,d1\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State AVOIDING_AGAIN]
         ,listens\<Zprime> = {resume, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TRYING_AGAINToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= TRYING_AGAIN \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AVOIDING_AGAINToFOUND =
  over MovementController
  pre "st= AVOIDING_AGAIN \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FOUND
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State FOUND]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AVOIDING_AGAINToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= AVOIDING_AGAIN \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AVOIDING_AGAINToAVOIDING =
  over MovementController
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= AVOIDING_AGAIN \<and> (since(evasionStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist()) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AVOIDING
         ,d0\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event changeDirection] @ [State AVOIDING]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AVOIDING_AGAINToGETTING_OUT =
  over MovementController
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= AVOIDING_AGAIN \<and> \<not>((since(evasionStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist())) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GETTING_OUT
         ,tr\<Zprime> =tr @ [Event shortRandomWalk] @ [State GETTING_OUT]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GETTING_OUTToFOUND =
  over MovementController
  pre "st= GETTING_OUT \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FOUND
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State FOUND]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GETTING_OUTToGOING =
  over MovementController
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {stop}, {turn}, {obstacle}}" 
  pre "st= GETTING_OUT \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GOING
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State GOING]
         ,listens\<Zprime> = {resume, stop, turn, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GETTING_OUTToWAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}" 
  pre "st= GETTING_OUT \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State WAITING]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_WAITING =
  over MovementController
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= WAITING \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= WAITING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_GOING =
  over MovementController
  params nxt \<in> "{{}, {resume}, {stop}, {turn}, {obstacle}}"
  pre "st= GOING \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= GOING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_AVOIDING =
  over MovementController
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}"
  pre "st= AVOIDING \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= AVOIDING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_TRYING_AGAIN =
  over MovementController
  params nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}"
  pre "st= TRYING_AGAIN \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= TRYING_AGAIN
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_GETTING_OUT =
  over MovementController
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}"
  pre "st= GETTING_OUT \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= GETTING_OUT
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "MovementController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine MovementControllerMachine =
  init Init
  invariant MovementController_inv
  operations  InitialToWAITING WAITINGToFOUND WAITINGToGOING WAITINGToWAITING GOINGToFOUND GOINGToGOING GOINGToAVOIDING GOINGToWAITING AVOIDINGToFOUND AVOIDINGToTRYING_AGAIN AVOIDINGToWAITING TRYING_AGAINToFOUND TRYING_AGAINToTRYING_AGAIN TRYING_AGAINToAVOIDING_AGAIN TRYING_AGAINToWAITING AVOIDING_AGAINToFOUND AVOIDING_AGAINToWAITING AVOIDING_AGAINToAVOIDING AVOIDING_AGAINToGETTING_OUT GETTING_OUTToFOUND GETTING_OUTToGOING GETTING_OUTToWAITING Idle_WAITING Idle_GOING Idle_AVOIDING Idle_TRYING_AGAIN Idle_GETTING_OUT
  until "st = FOUND"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes MovementController_inv"
  by zpog_full

lemma InitialToWAITING_inv [hoare_lemmas]: "InitialToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WAITINGToFOUND_inv [hoare_lemmas]: "WAITINGToFOUND() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WAITINGToGOING_inv [hoare_lemmas]: "WAITINGToGOING (a_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WAITINGToWAITING_inv [hoare_lemmas]: "WAITINGToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GOINGToFOUND_inv [hoare_lemmas]: "GOINGToFOUND() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GOINGToGOING_inv [hoare_lemmas]: "GOINGToGOING (a_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GOINGToAVOIDING_inv [hoare_lemmas]: "GOINGToAVOIDING (l_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GOINGToWAITING_inv [hoare_lemmas]: "GOINGToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AVOIDINGToFOUND_inv [hoare_lemmas]: "AVOIDINGToFOUND() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AVOIDINGToTRYING_AGAIN_inv [hoare_lemmas]: "AVOIDINGToTRYING_AGAIN (a_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AVOIDINGToWAITING_inv [hoare_lemmas]: "AVOIDINGToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TRYING_AGAINToFOUND_inv [hoare_lemmas]: "TRYING_AGAINToFOUND() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TRYING_AGAINToTRYING_AGAIN_inv [hoare_lemmas]: "TRYING_AGAINToTRYING_AGAIN (a_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TRYING_AGAINToAVOIDING_AGAIN_inv [hoare_lemmas]: "TRYING_AGAINToAVOIDING_AGAIN (l_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TRYING_AGAINToWAITING_inv [hoare_lemmas]: "TRYING_AGAINToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AVOIDING_AGAINToFOUND_inv [hoare_lemmas]: "AVOIDING_AGAINToFOUND() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AVOIDING_AGAINToWAITING_inv [hoare_lemmas]: "AVOIDING_AGAINToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AVOIDING_AGAINToAVOIDING_inv [hoare_lemmas]: "AVOIDING_AGAINToAVOIDING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AVOIDING_AGAINToGETTING_OUT_inv [hoare_lemmas]: "AVOIDING_AGAINToGETTING_OUT (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GETTING_OUTToFOUND_inv [hoare_lemmas]: "GETTING_OUTToFOUND() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GETTING_OUTToGOING_inv [hoare_lemmas]: "GETTING_OUTToGOING (a_input, nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GETTING_OUTToWAITING_inv [hoare_lemmas]: "GETTING_OUTToWAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_WAITING_inv [hoare_lemmas]: "Idle_WAITING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_GOING_inv [hoare_lemmas]: "Idle_GOING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_AVOIDING_inv [hoare_lemmas]: "Idle_AVOIDING (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_TRYING_AGAIN_inv [hoare_lemmas]: "Idle_TRYING_AGAIN (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_GETTING_OUT_inv [hoare_lemmas]: "Idle_GETTING_OUT (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WAITINGToFOUND() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WAITINGToGOING (a_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WAITINGToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GOINGToFOUND() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GOINGToGOING (a_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GOINGToAVOIDING (l_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GOINGToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDINGToFOUND() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDINGToTRYING_AGAIN (a_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDINGToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TRYING_AGAINToFOUND() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TRYING_AGAINToTRYING_AGAIN (a_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TRYING_AGAINToAVOIDING_AGAIN (l_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TRYING_AGAINToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDING_AGAINToFOUND() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDING_AGAINToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDING_AGAINToAVOIDING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AVOIDING_AGAINToGETTING_OUT (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GETTING_OUTToFOUND() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GETTING_OUTToGOING (a_input, nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GETTING_OUTToWAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_WAITING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_GOING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_AVOIDING (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_TRYING_AGAIN (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_GETTING_OUT (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "MovementController_axioms = True"

(* Terminal state(s), with no outgoing transition: FOUND.
   The machine's `until` clause exempts them from the deadlock-freedom
   obligation below: the machine has finished there. *)
lemma MovementController_deadlock_free: "MovementController_axioms  \<Longrightarrow> deadlock_free MovementControllerMachine"
  unfolding MovementControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
