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
   
enumtype St = Waiting | Going | Found | Avoiding | TryingAgain | AvoidingAgain | GettingOut | initial 
 


enumtype Evt = randomWalk | stop | flag | resume | turn | obstacle | changeDirection | shortRandomWalk 
 


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
record Chem = species :: int
record_default Chem
show_record Chem

record GasSensor = c :: Chem i :: real
record_default GasSensor
show_record GasSensor

definition A :: "Angle set" where [simp]: "A = UNIV"

definition L :: "Loc set" where [simp]: "L = UNIV"





text \<open> function definition \<close>

consts analysis :: " GasSensor list \<Rightarrow> Status"
consts intensity :: " GasSensor list \<Rightarrow> real"
consts location :: " GasSensor list \<Rightarrow> Angle"
consts insAtLeastThr :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts evadeStart :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckPeriod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts odometer :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore MovementController =
  a :: "Angle"
  d0 :: "real"
  d1 :: "real"
  l :: "Loc"
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
                      \<and> (st = Waiting \<longrightarrow> listens = {resume, stop, turn})
                      \<and> (st = Going \<longrightarrow> listens = {obstacle, turn, resume, stop})
                      \<and> (st = Found \<longrightarrow> listens = {stop, obstacle})
                      \<and> (st = Avoiding \<longrightarrow> listens = {stop, resume, turn})
                      \<and> (st = TryingAgain \<longrightarrow> listens = {stop, obstacle, resume, turn})
                      \<and> (st = AvoidingAgain \<longrightarrow> listens = {resume, stop})
                      \<and> (st = GettingOut \<longrightarrow> listens = {turn, resume, stop}) \<and> offered = listens"

subsection \<open> Operations \<close>

zoperation InitialToWaiting =
  over MovementController
  pre "st= initial"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation WaitingToFound =
  over MovementController
  pre "st= Waiting \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation WaitingToWaiting =
  over MovementController
  pre "st= Waiting \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation WaitingToGoing =
  over MovementController
  params a_input \<in> "A" 
  pre "st= Waiting \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {obstacle, turn, resume, stop}
         ,offered\<Zprime> = {obstacle, turn, resume, stop}
         ]"
        
zoperation GoingToFound =
  over MovementController
  pre "st= Going \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation GoingToWaiting =
  over MovementController
  pre "st= Going \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation GoingToGoing =
  over MovementController
  params a_input \<in> "A" 
  pre "st= Going \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {obstacle, turn, resume, stop}
         ,offered\<Zprime> = {obstacle, turn, resume, stop}
         ]"
        
zoperation GoingToAvoiding =
  over MovementController
  params l_input \<in> "L" 
  pre "st= Going \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,l\<Zprime> =l_input
         ,d0\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event changeDirection] @ [State Avoiding]
         ,listens\<Zprime> = {stop, resume, turn}
         ,offered\<Zprime> = {stop, resume, turn}
         ]"
        
zoperation FoundToFound =
  over MovementController
  pre "st= Found \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation FoundToFound_1 =
  over MovementController
  params l_input \<in> "L" 
  pre "st= Found \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,l\<Zprime> =l_input
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation AvoidingToFound =
  over MovementController
  pre "st= Avoiding \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation AvoidingToWaiting =
  over MovementController
  pre "st= Avoiding \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation AvoidingToTryingAgain =
  over MovementController
  params a_input \<in> "A" 
  pre "st= Avoiding \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {stop, obstacle, resume, turn}
         ,offered\<Zprime> = {stop, obstacle, resume, turn}
         ]"
        
zoperation TryingAgainToFound =
  over MovementController
  pre "st= TryingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation TryingAgainToWaiting =
  over MovementController
  pre "st= TryingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation TryingAgainToTryingAgain =
  over MovementController
  params a_input \<in> "A" 
  pre "st= TryingAgain \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {stop, obstacle, resume, turn}
         ,offered\<Zprime> = {stop, obstacle, resume, turn}
         ]"
        
zoperation TryingAgainToAvoidingAgain =
  over MovementController
  params l_input \<in> "L" 
  pre "st= TryingAgain \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AvoidingAgain
         ,l\<Zprime> =l_input
         ,d1\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State AvoidingAgain]
         ,listens\<Zprime> = {resume, stop}
         ,offered\<Zprime> = {resume, stop}
         ]"
        
zoperation AvoidingAgainToFound =
  over MovementController
  pre "st= AvoidingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation AvoidingAgainToWaiting =
  over MovementController
  pre "st= AvoidingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation AvoidingAgainToAvoiding =
  over MovementController
  pre "st= AvoidingAgain \<and> (since(evadeStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist()) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,d0\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event changeDirection] @ [State Avoiding]
         ,listens\<Zprime> = {stop, resume, turn}
         ,offered\<Zprime> = {stop, resume, turn}
         ]"
        
zoperation AvoidingAgainToGettingOut =
  over MovementController
  pre "st= AvoidingAgain \<and> \<not>since(evadeStart())<stuckPeriod() \<and> \<not>(d1 - d0)>stuckDist() \<and> \<not>((since(evadeStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist())) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GettingOut
         ,tr\<Zprime> =tr @ [Event shortRandomWalk] @ [State GettingOut]
         ,listens\<Zprime> = {turn, resume, stop}
         ,offered\<Zprime> = {turn, resume, stop}
         ]"
        
zoperation GettingOutToFound =
  over MovementController
  pre "st= GettingOut \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop, obstacle}
         ,offered\<Zprime> = {stop, obstacle}
         ]"
        
zoperation GettingOutToWaiting =
  over MovementController
  pre "st= GettingOut \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = {resume, stop, turn}
         ]"
        
zoperation GettingOutToGoing =
  over MovementController
  params a_input \<in> "A" 
  pre "st= GettingOut \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {obstacle, turn, resume, stop}
         ,offered\<Zprime> = {obstacle, turn, resume, stop}
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
  operations  InitialToWaiting WaitingToFound WaitingToWaiting WaitingToGoing GoingToFound GoingToWaiting GoingToGoing GoingToAvoiding FoundToFound FoundToFound_1 AvoidingToFound AvoidingToWaiting AvoidingToTryingAgain TryingAgainToFound TryingAgainToWaiting TryingAgainToTryingAgain TryingAgainToAvoidingAgain AvoidingAgainToFound AvoidingAgainToWaiting AvoidingAgainToAvoiding AvoidingAgainToGettingOut GettingOutToFound GettingOutToWaiting GettingOutToGoing


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes MovementController_inv"
  by zpog_full

lemma InitialToWaiting_inv [hoare_lemmas]: "InitialToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToFound_inv [hoare_lemmas]: "WaitingToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToWaiting_inv [hoare_lemmas]: "WaitingToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToGoing_inv [hoare_lemmas]: "WaitingToGoing (a_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToFound_inv [hoare_lemmas]: "GoingToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GoingToWaiting_inv [hoare_lemmas]: "GoingToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GoingToGoing_inv [hoare_lemmas]: "GoingToGoing (a_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToAvoiding_inv [hoare_lemmas]: "GoingToAvoiding (l_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FoundToFound_inv [hoare_lemmas]: "FoundToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma FoundToFound_1_inv [hoare_lemmas]: "FoundToFound_1 (l_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToFound_inv [hoare_lemmas]: "AvoidingToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingToWaiting_inv [hoare_lemmas]: "AvoidingToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingToTryingAgain_inv [hoare_lemmas]: "AvoidingToTryingAgain (a_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToFound_inv [hoare_lemmas]: "TryingAgainToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TryingAgainToWaiting_inv [hoare_lemmas]: "TryingAgainToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TryingAgainToTryingAgain_inv [hoare_lemmas]: "TryingAgainToTryingAgain (a_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToAvoidingAgain_inv [hoare_lemmas]: "TryingAgainToAvoidingAgain (l_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToFound_inv [hoare_lemmas]: "AvoidingAgainToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToWaiting_inv [hoare_lemmas]: "AvoidingAgainToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToAvoiding_inv [hoare_lemmas]: "AvoidingAgainToAvoiding() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToGettingOut_inv [hoare_lemmas]: "AvoidingAgainToGettingOut() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToFound_inv [hoare_lemmas]: "GettingOutToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToWaiting_inv [hoare_lemmas]: "GettingOutToWaiting() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToGoing_inv [hoare_lemmas]: "GettingOutToGoing (a_input) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WaitingToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WaitingToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WaitingToGoing (a_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToGoing (a_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToAvoiding (l_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "FoundToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "FoundToFound_1 (l_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToTryingAgain (a_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToTryingAgain (a_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToAvoidingAgain (l_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToAvoiding() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToGettingOut() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToWaiting() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToGoing (a_input) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "MovementController_axioms = True"

lemma MovementController_deadlock_free: "MovementController_axioms  \<Longrightarrow> deadlock_free MovementControllerMachine"
  unfolding MovementControllerMachine_def
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
