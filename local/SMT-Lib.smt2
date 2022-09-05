(declare-fun n () Int)
(assert ( and ( > n 0 ) ( >= 0 n ) ) )
(check-sat)(get-model)