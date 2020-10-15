main {
  LI 8 $a0
  JAL fibonacci
  MV $v0 $a0
  JAL printi
  SYS OS_EXIT
}
fibonacci {
  LI 2 $t0
  LT $a0 $t0 $t0
  BIZ $t0 3
  LI 1 $v0
  RET
  PSH $a0
  PSH $ra
  SBI $a0 1 $a0
  JAL fibonacci
  POP $ra
  POP $t0
  ADD $v0 $t0 $v0
  RET
}
printi {
  DVI $a0 10
  ADI $hi 48 $a0
  SYS OS_PRINT
  BIZ $lo 5
  MV $lo $a0
  PSH $ra
  JAL printi
  POP $ra
  RET
}