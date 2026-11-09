theory SynController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the SynController state machine in Z Machine notations.\<close>

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
   
enumtype St = M1 | M2 | M3 | M4 | M5 | M6 | M7 | M8 | M9 | M10 | M11 | M12 | M13 | M14 | M15 | M16 | M17 | M18 | M19 | Final | initial 
 


enumtype Evt = endTask | move | advance | retreat | tick 
 


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

consts reading1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold1 :: "unit \<Rightarrow> real" where "threshold1 u = 0.75"  (* named constant; value from the model *)
consts reading2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold2 :: "unit \<Rightarrow> real" where "threshold2 u = 1"  (* named constant; value from the model *)
consts reading3 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold3 :: "unit \<Rightarrow> real" where "threshold3 u = 1.25"  (* named constant; value from the model *)
consts reading4 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold4 :: "unit \<Rightarrow> real" where "threshold4 u = 1.5"  (* named constant; value from the model *)
consts reading5 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold5 :: "unit \<Rightarrow> real" where "threshold5 u = 1.75"  (* named constant; value from the model *)
consts reading6 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold6 :: "unit \<Rightarrow> real" where "threshold6 u = 2"  (* named constant; value from the model *)
consts reading7 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold7 :: "unit \<Rightarrow> real" where "threshold7 u = 2.25"  (* named constant; value from the model *)
consts reading8 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold8 :: "unit \<Rightarrow> real" where "threshold8 u = 2.5"  (* named constant; value from the model *)
consts reading9 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold9 :: "unit \<Rightarrow> real" where "threshold9 u = 2.75"  (* named constant; value from the model *)
consts reading10 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold10 :: "unit \<Rightarrow> real" where "threshold10 u = 3"  (* named constant; value from the model *)
consts reading11 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold11 :: "unit \<Rightarrow> real" where "threshold11 u = 3.25"  (* named constant; value from the model *)
consts reading12 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold12 :: "unit \<Rightarrow> real" where "threshold12 u = 3.5"  (* named constant; value from the model *)
consts reading13 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold13 :: "unit \<Rightarrow> real" where "threshold13 u = 3.75"  (* named constant; value from the model *)
consts reading14 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold14 :: "unit \<Rightarrow> real" where "threshold14 u = 4"  (* named constant; value from the model *)
consts reading15 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold15 :: "unit \<Rightarrow> real" where "threshold15 u = 4.25"  (* named constant; value from the model *)
consts reading16 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold16 :: "unit \<Rightarrow> real" where "threshold16 u = 4.5"  (* named constant; value from the model *)
consts reading17 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold17 :: "unit \<Rightarrow> real" where "threshold17 u = 4.75"  (* named constant; value from the model *)
consts reading18 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold18 :: "unit \<Rightarrow> real" where "threshold18 u = 5"  (* named constant; value from the model *)
consts reading19 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold19 :: "unit \<Rightarrow> real" where "threshold19 u = 5.25"  (* named constant; value from the model *)

subsection \<open> State Space \<close>

zstore SynController =
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
                      \<and> (st = M1 \<longrightarrow> listens = {advance, tick, retreat, endTask})
                      \<and> (st = M2 \<longrightarrow> listens = {tick, endTask, advance, retreat})
                      \<and> (st = M3 \<longrightarrow> listens = {tick, endTask, retreat, advance})
                      \<and> (st = M4 \<longrightarrow> listens = {retreat, tick, endTask, advance})
                      \<and> (st = M5 \<longrightarrow> listens = {retreat, tick, endTask, advance})
                      \<and> (st = M6 \<longrightarrow> listens = {retreat, tick, advance, endTask})
                      \<and> (st = M7 \<longrightarrow> listens = {tick, endTask, retreat, advance})
                      \<and> (st = M8 \<longrightarrow> listens = {advance, tick, endTask, retreat})
                      \<and> (st = M9 \<longrightarrow> listens = {endTask, advance, retreat, tick})
                      \<and> (st = M10 \<longrightarrow> listens = {endTask, tick, advance, retreat})
                      \<and> (st = M11 \<longrightarrow> listens = {advance, tick, retreat, endTask})
                      \<and> (st = M12 \<longrightarrow> listens = {advance, tick, endTask, retreat})
                      \<and> (st = M13 \<longrightarrow> listens = {retreat, endTask, tick, advance})
                      \<and> (st = M14 \<longrightarrow> listens = {endTask, tick, retreat, advance})
                      \<and> (st = M15 \<longrightarrow> listens = {advance, tick, retreat, endTask})
                      \<and> (st = M16 \<longrightarrow> listens = {tick, retreat, advance, endTask})
                      \<and> (st = M17 \<longrightarrow> listens = {advance, tick, endTask, retreat})
                      \<and> (st = M18 \<longrightarrow> listens = {retreat, endTask, advance, tick})
                      \<and> (st = M19 \<longrightarrow> listens = {tick, retreat, advance, endTask})
                      \<and> (st = Final \<longrightarrow> listens = {tick})
                                                                               \<and> (st = M1 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {endTask}))
                                                                               \<and> (st = M2 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {advance} \<or> offered = {retreat}))
                                                                               \<and> (st = M3 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {retreat} \<or> offered = {advance}))
                                                                               \<and> (st = M4 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {advance}))
                                                                               \<and> (st = M5 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {advance}))
                                                                               \<and> (st = M6 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {tick} \<or> offered = {advance} \<or> offered = {endTask}))
                                                                               \<and> (st = M7 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {retreat} \<or> offered = {advance}))
                                                                               \<and> (st = M8 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {retreat}))
                                                                               \<and> (st = M9 \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {advance} \<or> offered = {retreat} \<or> offered = {tick}))
                                                                               \<and> (st = M10 \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {tick} \<or> offered = {advance} \<or> offered = {retreat}))
                                                                               \<and> (st = M11 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {endTask}))
                                                                               \<and> (st = M12 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {retreat}))
                                                                               \<and> (st = M13 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {endTask} \<or> offered = {tick} \<or> offered = {advance}))
                                                                               \<and> (st = M14 \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {advance}))
                                                                               \<and> (st = M15 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {endTask}))
                                                                               \<and> (st = M16 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {advance} \<or> offered = {endTask}))
                                                                               \<and> (st = M17 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {endTask} \<or> offered = {retreat}))
                                                                               \<and> (st = M18 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {endTask} \<or> offered = {advance} \<or> offered = {tick}))
                                                                               \<and> (st = M19 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {advance} \<or> offered = {endTask}))
                                                                               \<and> (st = Final \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToM1 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= initial"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr  @ [State M1]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M1 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToM2 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {advance}, {retreat}}" 
  pre "st= M1 \<and> reading1()\<le>threshold1() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M2]
         ,listens\<Zprime> = {tick, endTask, advance, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToM1 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M1 \<and> \<not>reading1()\<le>threshold1() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M1]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToM1_1 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M1 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event tick] @ [State M1]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M2 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToM3 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}" 
  pre "st= M2 \<and> reading2()\<le>threshold2() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M3]
         ,listens\<Zprime> = {tick, endTask, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToM1 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M2 \<and> \<not>reading2()\<le>threshold2() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M1]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToM2 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {advance}, {retreat}}" 
  pre "st= M2 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> =tr @ [Event tick] @ [State M2]
         ,listens\<Zprime> = {tick, endTask, advance, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M3 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToM4 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}" 
  pre "st= M3 \<and> reading3()\<le>threshold3() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M4]
         ,listens\<Zprime> = {retreat, tick, endTask, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToM2 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {advance}, {retreat}}" 
  pre "st= M3 \<and> \<not>reading3()\<le>threshold3() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M2]
         ,listens\<Zprime> = {tick, endTask, advance, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToM3 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}" 
  pre "st= M3 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> =tr @ [Event tick] @ [State M3]
         ,listens\<Zprime> = {tick, endTask, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M4 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToM5 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}" 
  pre "st= M4 \<and> reading4()\<le>threshold4() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M5]
         ,listens\<Zprime> = {retreat, tick, endTask, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToM3 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}" 
  pre "st= M4 \<and> \<not>reading4()\<le>threshold4() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M3]
         ,listens\<Zprime> = {tick, endTask, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToM4 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}" 
  pre "st= M4 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> =tr @ [Event tick] @ [State M4]
         ,listens\<Zprime> = {retreat, tick, endTask, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M5 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToM6 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}" 
  pre "st= M5 \<and> reading5()\<le>threshold5() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M6]
         ,listens\<Zprime> = {retreat, tick, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToM4 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}" 
  pre "st= M5 \<and> \<not>reading5()\<le>threshold5() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M4]
         ,listens\<Zprime> = {retreat, tick, endTask, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToM5 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}" 
  pre "st= M5 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> =tr @ [Event tick] @ [State M5]
         ,listens\<Zprime> = {retreat, tick, endTask, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M6 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToM7 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}" 
  pre "st= M6 \<and> reading6()\<le>threshold6() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M7]
         ,listens\<Zprime> = {tick, endTask, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToM5 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}" 
  pre "st= M6 \<and> \<not>reading6()\<le>threshold6() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M5]
         ,listens\<Zprime> = {retreat, tick, endTask, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToM6 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}" 
  pre "st= M6 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> =tr @ [Event tick] @ [State M6]
         ,listens\<Zprime> = {retreat, tick, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M7 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToM8 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M7 \<and> reading7()\<le>threshold7() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M8]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToM6 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}" 
  pre "st= M7 \<and> \<not>reading7()\<le>threshold7() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M6]
         ,listens\<Zprime> = {retreat, tick, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToM7 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}" 
  pre "st= M7 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> =tr @ [Event tick] @ [State M7]
         ,listens\<Zprime> = {tick, endTask, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M8 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToM9 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}" 
  pre "st= M8 \<and> reading8()\<le>threshold8() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M9]
         ,listens\<Zprime> = {endTask, advance, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToM7 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}" 
  pre "st= M8 \<and> \<not>reading8()\<le>threshold8() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M7]
         ,listens\<Zprime> = {tick, endTask, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToM8 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M8 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> =tr @ [Event tick] @ [State M8]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M9 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToM10 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {advance}, {retreat}}" 
  pre "st= M9 \<and> reading9()\<le>threshold9() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M10]
         ,listens\<Zprime> = {endTask, tick, advance, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToM8 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M9 \<and> \<not>reading9()\<le>threshold9() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M8]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToM9 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}" 
  pre "st= M9 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> =tr @ [Event tick] @ [State M9]
         ,listens\<Zprime> = {endTask, advance, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M10 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToM11 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M10 \<and> reading10()\<le>threshold10() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M11]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToM9 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}" 
  pre "st= M10 \<and> \<not>reading10()\<le>threshold10() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M9]
         ,listens\<Zprime> = {endTask, advance, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToM10 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {advance}, {retreat}}" 
  pre "st= M10 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> =tr @ [Event tick] @ [State M10]
         ,listens\<Zprime> = {endTask, tick, advance, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M11 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToM12 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M11 \<and> reading11()\<le>threshold11() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M12
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M12]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToM10 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {advance}, {retreat}}" 
  pre "st= M11 \<and> \<not>reading11()\<le>threshold11() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M10]
         ,listens\<Zprime> = {endTask, tick, advance, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToM11 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M11 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> =tr @ [Event tick] @ [State M11]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M12ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M12 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M12ToM13 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {tick}, {advance}}" 
  pre "st= M12 \<and> reading12()\<le>threshold12() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M13
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M13]
         ,listens\<Zprime> = {retreat, endTask, tick, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M12ToM11 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M12 \<and> \<not>reading12()\<le>threshold12() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M11]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M12ToM12 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M12 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M12
         ,tr\<Zprime> =tr @ [Event tick] @ [State M12]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M13ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M13 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M13ToM14 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {retreat}, {advance}}" 
  pre "st= M13 \<and> reading13()\<le>threshold13() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M14
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M14]
         ,listens\<Zprime> = {endTask, tick, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M13ToM12 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M13 \<and> \<not>reading13()\<le>threshold13() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M12
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M12]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M13ToM13 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {tick}, {advance}}" 
  pre "st= M13 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M13
         ,tr\<Zprime> =tr @ [Event tick] @ [State M13]
         ,listens\<Zprime> = {retreat, endTask, tick, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M14ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M14 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M14ToM15 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M14 \<and> reading14()\<le>threshold14() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M15
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M15]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M14ToM13 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {tick}, {advance}}" 
  pre "st= M14 \<and> \<not>reading14()\<le>threshold14() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M13
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M13]
         ,listens\<Zprime> = {retreat, endTask, tick, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M14ToM14 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {retreat}, {advance}}" 
  pre "st= M14 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M14
         ,tr\<Zprime> =tr @ [Event tick] @ [State M14]
         ,listens\<Zprime> = {endTask, tick, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M15ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M15 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M15ToM16 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M15 \<and> reading15()\<le>threshold15() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M16
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M16]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M15ToM14 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {retreat}, {advance}}" 
  pre "st= M15 \<and> \<not>reading15()\<le>threshold15() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M14
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M14]
         ,listens\<Zprime> = {endTask, tick, retreat, advance}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M15ToM15 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M15 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M15
         ,tr\<Zprime> =tr @ [Event tick] @ [State M15]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M16ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M16 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M16ToM17 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M16 \<and> reading16()\<le>threshold16() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M17
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M17]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M16ToM15 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M16 \<and> \<not>reading16()\<le>threshold16() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M15
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M15]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M16ToM16 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M16 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M16
         ,tr\<Zprime> =tr @ [Event tick] @ [State M16]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M17ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M17 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M17ToM18 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}" 
  pre "st= M17 \<and> reading17()\<le>threshold17() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M18
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M18]
         ,listens\<Zprime> = {retreat, endTask, advance, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M17ToM16 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M17 \<and> \<not>reading17()\<le>threshold17() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M16
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M16]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M17ToM17 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M17 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M17
         ,tr\<Zprime> =tr @ [Event tick] @ [State M17]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M18ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M18 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M18ToM19 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M18 \<and> reading18()\<le>threshold18() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M19
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M19]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M18ToM17 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}" 
  pre "st= M18 \<and> \<not>reading18()\<le>threshold18() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M17
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M17]
         ,listens\<Zprime> = {advance, tick, endTask, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M18ToM18 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}" 
  pre "st= M18 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M18
         ,tr\<Zprime> =tr @ [Event tick] @ [State M18]
         ,listens\<Zprime> = {retreat, endTask, advance, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M19ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M19 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M19ToM1 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}" 
  pre "st= M19 \<and> reading19()\<le>threshold19() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M1]
         ,listens\<Zprime> = {advance, tick, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M19ToM18 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}" 
  pre "st= M19 \<and> \<not>reading19()\<le>threshold19() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M18
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M18]
         ,listens\<Zprime> = {retreat, endTask, advance, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M19ToM19 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M19 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M19
         ,tr\<Zprime> =tr @ [Event tick] @ [State M19]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FinalToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Final \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_M1 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}"
  pre "st= M1 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading1()\<le>threshold1() \<and> advance \<in> offered) \<and> \<not>(\<not>reading1()\<le>threshold1() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M2 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {advance}, {retreat}}"
  pre "st= M2 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading2()\<le>threshold2() \<and> advance \<in> offered) \<and> \<not>(\<not>reading2()\<le>threshold2() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M3 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}"
  pre "st= M3 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading3()\<le>threshold3() \<and> advance \<in> offered) \<and> \<not>(\<not>reading3()\<le>threshold3() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M4 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}"
  pre "st= M4 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading4()\<le>threshold4() \<and> advance \<in> offered) \<and> \<not>(\<not>reading4()\<le>threshold4() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M5 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {endTask}, {advance}}"
  pre "st= M5 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading5()\<le>threshold5() \<and> advance \<in> offered) \<and> \<not>(\<not>reading5()\<le>threshold5() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M6 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}"
  pre "st= M6 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading6()\<le>threshold6() \<and> advance \<in> offered) \<and> \<not>(\<not>reading6()\<le>threshold6() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M7 =
  over SynController
  params nxt \<in> "{{}, {tick}, {endTask}, {retreat}, {advance}}"
  pre "st= M7 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading7()\<le>threshold7() \<and> advance \<in> offered) \<and> \<not>(\<not>reading7()\<le>threshold7() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M8 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}"
  pre "st= M8 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading8()\<le>threshold8() \<and> advance \<in> offered) \<and> \<not>(\<not>reading8()\<le>threshold8() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M9 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}"
  pre "st= M9 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading9()\<le>threshold9() \<and> advance \<in> offered) \<and> \<not>(\<not>reading9()\<le>threshold9() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M10 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {advance}, {retreat}}"
  pre "st= M10 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading10()\<le>threshold10() \<and> advance \<in> offered) \<and> \<not>(\<not>reading10()\<le>threshold10() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M11 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}"
  pre "st= M11 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading11()\<le>threshold11() \<and> advance \<in> offered) \<and> \<not>(\<not>reading11()\<le>threshold11() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M12 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}"
  pre "st= M12 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading12()\<le>threshold12() \<and> advance \<in> offered) \<and> \<not>(\<not>reading12()\<le>threshold12() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M12
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M13 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {tick}, {advance}}"
  pre "st= M13 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading13()\<le>threshold13() \<and> advance \<in> offered) \<and> \<not>(\<not>reading13()\<le>threshold13() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M13
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M14 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {tick}, {retreat}, {advance}}"
  pre "st= M14 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading14()\<le>threshold14() \<and> advance \<in> offered) \<and> \<not>(\<not>reading14()\<le>threshold14() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M14
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M15 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {retreat}, {endTask}}"
  pre "st= M15 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading15()\<le>threshold15() \<and> advance \<in> offered) \<and> \<not>(\<not>reading15()\<le>threshold15() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M15
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M16 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}"
  pre "st= M16 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading16()\<le>threshold16() \<and> advance \<in> offered) \<and> \<not>(\<not>reading16()\<le>threshold16() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M16
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M17 =
  over SynController
  params nxt \<in> "{{}, {advance}, {tick}, {endTask}, {retreat}}"
  pre "st= M17 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading17()\<le>threshold17() \<and> advance \<in> offered) \<and> \<not>(\<not>reading17()\<le>threshold17() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M17
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M18 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}"
  pre "st= M18 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading18()\<le>threshold18() \<and> advance \<in> offered) \<and> \<not>(\<not>reading18()\<le>threshold18() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M18
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M19 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}"
  pre "st= M19 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading19()\<le>threshold19() \<and> advance \<in> offered) \<and> \<not>(\<not>reading19()\<le>threshold19() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M19
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Final =
  over SynController
  params nxt \<in> "{{}, {tick}}"
  pre "st= Final \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "SynController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SynControllerMachine =
  init Init
  invariant SynController_inv
  operations  InitialToM1 M1ToFinal M1ToM2 M1ToM1 M1ToM1_1 M2ToFinal M2ToM3 M2ToM1 M2ToM2 M3ToFinal M3ToM4 M3ToM2 M3ToM3 M4ToFinal M4ToM5 M4ToM3 M4ToM4 M5ToFinal M5ToM6 M5ToM4 M5ToM5 M6ToFinal M6ToM7 M6ToM5 M6ToM6 M7ToFinal M7ToM8 M7ToM6 M7ToM7 M8ToFinal M8ToM9 M8ToM7 M8ToM8 M9ToFinal M9ToM10 M9ToM8 M9ToM9 M10ToFinal M10ToM11 M10ToM9 M10ToM10 M11ToFinal M11ToM12 M11ToM10 M11ToM11 M12ToFinal M12ToM13 M12ToM11 M12ToM12 M13ToFinal M13ToM14 M13ToM12 M13ToM13 M14ToFinal M14ToM15 M14ToM13 M14ToM14 M15ToFinal M15ToM16 M15ToM14 M15ToM15 M16ToFinal M16ToM17 M16ToM15 M16ToM16 M17ToFinal M17ToM18 M17ToM16 M17ToM17 M18ToFinal M18ToM19 M18ToM17 M18ToM18 M19ToFinal M19ToM1 M19ToM18 M19ToM19 FinalToFinal Idle_M1 Idle_M2 Idle_M3 Idle_M4 Idle_M5 Idle_M6 Idle_M7 Idle_M8 Idle_M9 Idle_M10 Idle_M11 Idle_M12 Idle_M13 Idle_M14 Idle_M15 Idle_M16 Idle_M17 Idle_M18 Idle_M19 Idle_Final


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SynController_inv"
  by zpog_full

lemma InitialToM1_inv [hoare_lemmas]: "InitialToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToFinal_inv [hoare_lemmas]: "M1ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToM2_inv [hoare_lemmas]: "M1ToM2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToM1_inv [hoare_lemmas]: "M1ToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToM1_1_inv [hoare_lemmas]: "M1ToM1_1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToFinal_inv [hoare_lemmas]: "M2ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToM3_inv [hoare_lemmas]: "M2ToM3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToM1_inv [hoare_lemmas]: "M2ToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToM2_inv [hoare_lemmas]: "M2ToM2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToFinal_inv [hoare_lemmas]: "M3ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToM4_inv [hoare_lemmas]: "M3ToM4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToM2_inv [hoare_lemmas]: "M3ToM2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToM3_inv [hoare_lemmas]: "M3ToM3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToFinal_inv [hoare_lemmas]: "M4ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToM5_inv [hoare_lemmas]: "M4ToM5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToM3_inv [hoare_lemmas]: "M4ToM3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToM4_inv [hoare_lemmas]: "M4ToM4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToFinal_inv [hoare_lemmas]: "M5ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToM6_inv [hoare_lemmas]: "M5ToM6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToM4_inv [hoare_lemmas]: "M5ToM4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToM5_inv [hoare_lemmas]: "M5ToM5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToFinal_inv [hoare_lemmas]: "M6ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToM7_inv [hoare_lemmas]: "M6ToM7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToM5_inv [hoare_lemmas]: "M6ToM5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToM6_inv [hoare_lemmas]: "M6ToM6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToFinal_inv [hoare_lemmas]: "M7ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToM8_inv [hoare_lemmas]: "M7ToM8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToM6_inv [hoare_lemmas]: "M7ToM6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToM7_inv [hoare_lemmas]: "M7ToM7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToFinal_inv [hoare_lemmas]: "M8ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToM9_inv [hoare_lemmas]: "M8ToM9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToM7_inv [hoare_lemmas]: "M8ToM7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToM8_inv [hoare_lemmas]: "M8ToM8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToFinal_inv [hoare_lemmas]: "M9ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToM10_inv [hoare_lemmas]: "M9ToM10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToM8_inv [hoare_lemmas]: "M9ToM8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToM9_inv [hoare_lemmas]: "M9ToM9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToFinal_inv [hoare_lemmas]: "M10ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToM11_inv [hoare_lemmas]: "M10ToM11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToM9_inv [hoare_lemmas]: "M10ToM9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToM10_inv [hoare_lemmas]: "M10ToM10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToFinal_inv [hoare_lemmas]: "M11ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToM12_inv [hoare_lemmas]: "M11ToM12 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToM10_inv [hoare_lemmas]: "M11ToM10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToM11_inv [hoare_lemmas]: "M11ToM11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M12ToFinal_inv [hoare_lemmas]: "M12ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M12ToM13_inv [hoare_lemmas]: "M12ToM13 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M12ToM11_inv [hoare_lemmas]: "M12ToM11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M12ToM12_inv [hoare_lemmas]: "M12ToM12 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M13ToFinal_inv [hoare_lemmas]: "M13ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M13ToM14_inv [hoare_lemmas]: "M13ToM14 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M13ToM12_inv [hoare_lemmas]: "M13ToM12 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M13ToM13_inv [hoare_lemmas]: "M13ToM13 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M14ToFinal_inv [hoare_lemmas]: "M14ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M14ToM15_inv [hoare_lemmas]: "M14ToM15 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M14ToM13_inv [hoare_lemmas]: "M14ToM13 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M14ToM14_inv [hoare_lemmas]: "M14ToM14 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M15ToFinal_inv [hoare_lemmas]: "M15ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M15ToM16_inv [hoare_lemmas]: "M15ToM16 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M15ToM14_inv [hoare_lemmas]: "M15ToM14 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M15ToM15_inv [hoare_lemmas]: "M15ToM15 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M16ToFinal_inv [hoare_lemmas]: "M16ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M16ToM17_inv [hoare_lemmas]: "M16ToM17 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M16ToM15_inv [hoare_lemmas]: "M16ToM15 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M16ToM16_inv [hoare_lemmas]: "M16ToM16 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M17ToFinal_inv [hoare_lemmas]: "M17ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M17ToM18_inv [hoare_lemmas]: "M17ToM18 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M17ToM16_inv [hoare_lemmas]: "M17ToM16 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M17ToM17_inv [hoare_lemmas]: "M17ToM17 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M18ToFinal_inv [hoare_lemmas]: "M18ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M18ToM19_inv [hoare_lemmas]: "M18ToM19 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M18ToM17_inv [hoare_lemmas]: "M18ToM17 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M18ToM18_inv [hoare_lemmas]: "M18ToM18 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M19ToFinal_inv [hoare_lemmas]: "M19ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M19ToM1_inv [hoare_lemmas]: "M19ToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M19ToM18_inv [hoare_lemmas]: "M19ToM18 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M19ToM19_inv [hoare_lemmas]: "M19ToM19 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M1_inv [hoare_lemmas]: "Idle_M1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M2_inv [hoare_lemmas]: "Idle_M2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M3_inv [hoare_lemmas]: "Idle_M3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M4_inv [hoare_lemmas]: "Idle_M4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M5_inv [hoare_lemmas]: "Idle_M5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M6_inv [hoare_lemmas]: "Idle_M6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M7_inv [hoare_lemmas]: "Idle_M7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M8_inv [hoare_lemmas]: "Idle_M8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M9_inv [hoare_lemmas]: "Idle_M9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M10_inv [hoare_lemmas]: "Idle_M10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M11_inv [hoare_lemmas]: "Idle_M11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M12_inv [hoare_lemmas]: "Idle_M12 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M13_inv [hoare_lemmas]: "Idle_M13 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M14_inv [hoare_lemmas]: "Idle_M14 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M15_inv [hoare_lemmas]: "Idle_M15 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M16_inv [hoare_lemmas]: "Idle_M16 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M17_inv [hoare_lemmas]: "Idle_M17 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M18_inv [hoare_lemmas]: "Idle_M18 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M19_inv [hoare_lemmas]: "Idle_M19 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Final_inv [hoare_lemmas]: "Idle_Final (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToM2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToM1_1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToM3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToM2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToM4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToM2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToM3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToM5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToM3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToM4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToM6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToM4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToM5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToM7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToM5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToM6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToM8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToM6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToM7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToM9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToM7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToM8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToM10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToM8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToM9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToM11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToM9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToM10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToM12 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToM10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToM11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M12ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M12ToM13 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M12ToM11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M12ToM12 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M13ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M13ToM14 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M13ToM12 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M13ToM13 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M14ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M14ToM15 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M14ToM13 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M14ToM14 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M15ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M15ToM16 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M15ToM14 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M15ToM15 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M16ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M16ToM17 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M16ToM15 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M16ToM16 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M17ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M17ToM18 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M17ToM16 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M17ToM17 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M18ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M18ToM19 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M18ToM17 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M18ToM18 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M19ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M19ToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M19ToM18 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M19ToM19 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M12 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M13 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M14 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M15 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M16 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M17 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M18 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M19 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Final (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SynController_axioms = True"

lemma SynController_deadlock_free: "SynController_axioms  \<Longrightarrow> deadlock_free SynControllerMachine"
  unfolding SynControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
