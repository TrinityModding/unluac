.version	5.4

.format	0
.instruction_size	4
.integer_format	8
.float_format	8
.endianness	LITTLE

.function	main

.source	null
.linedefined	0
.lastlinedefined	0
.numparams	0
.is_vararg	1
.maxstacksize	5

.upvalue	"_ENV"	0	true

.constant	k0	"length"
.constant	k1	0
.constant	k2	"Normal_0"
.constant	k3	"Normal_1"
.constant	k4	"Cute"
.constant	k5	"Wool"
.constant	k6	"Metal"
.constant	k7	"Cool"

varargprep     0
newtable      r0     0     0     0
extraarg       0
setfield      r0    k0    k1 ; k1 = 0
seti          r0     1    k2 ; k2 = "Normal_0"
seti          r0     2    k3 ; k3 = "Normal_1"
seti          r0     3    k4 ; k4 = "Cute"
seti          r0     4    k5 ; k5 = "Wool"
seti          r0     5    k6 ; k6 = "Metal"
seti          r0     6    k7 ; k7 = "Cool"
loadi         r1     2
closure       r2    f0
move          r1    r2
move          r2    r1
geti          r3    r0     1
geti          r4    r0     1
call          r2     3     2
move          r1    r2
return        r2     1     1     0

.function	main/f0

.source	null
.linedefined	11
.lastlinedefined	21
.numparams	2
.is_vararg	0
.maxstacksize	8

.upvalue	null	0	false

.constant	k0	" and "
.constant	k1	"print"
.constant	k2	"This gets used twice "
.constant	k3	L"This is a weird function, right? AnArray[1] = "

move          r2    r0
loadk         r3    k0 ; k0 = " and "
move          r4    r1
concat        r2     3
gettabup      r3    u0    k1 ; k1 = "print"
loadk         r4    k2 ; k2 = "This gets used twice" (truncated)
move          r5    r2
concat        r4     2
call          r3     2     1
loadi         r3     0
loadi         r3     3
loadi         r3    10
loadi         r4     2
move          r3    r4
gettabup      r5    u0    k1 ; k1 = "print"
loadk         r6    k3 ; k3 = L"This is a weird func" (truncated)
move          r7    r0
concat        r6     2
call          r5     2     1
loadi         r5     5
return1       r5     2     0     0
return0       r5     1     0     0

