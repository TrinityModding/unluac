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
.maxstacksize	6

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
loadi         r5     7
call          r2     4     2
move          r1    r2
return        r2     1     1     0

.function	main/f0

.source	null
.linedefined	11
.lastlinedefined	30
.numparams	3
.is_vararg	0
.maxstacksize	9

.upvalue	null	0	false

.constant	k0	"print"
.constant	k1	"x is less than 10"
.constant	k2	"value3 < 32767"
.constant	k3	" and "
.constant	k4	"This gets used twice "
.constant	k5	L"This is a weird function, right? AnArray[1] = "

.label	l1
lti           r2    10     0     0
jmp           l9
gettabup      r3    u0    k0 ; k0 = "print"
loadk         r4    k1 ; k1 = "x is less than 10"
call          r3     2     1
addi          r2    r2     1
mmbini        r2     1     6     0
jmp           l1
.label	l9
loadi         r3 32767
lt            r2    r3     0     0
jmp          l15
gettabup      r3    u0    k0 ; k0 = "print"
loadk         r4    k2 ; k2 = "value3 < 32767"
call          r3     2     1
.label	l15
move          r3    r0
loadk         r4    k3 ; k3 = " and "
move          r5    r1
concat        r3     3
gettabup      r4    u0    k0 ; k0 = "print"
loadk         r5    k4 ; k4 = "This gets used twice" (truncated)
move          r6    r3
concat        r5     2
call          r4     2     1
loadi         r4     0
loadi         r4     3
loadi         r4    10
loadi         r5     2
move          r4    r5
gettabup      r6    u0    k0 ; k0 = "print"
loadk         r7    k5 ; k5 = L"This is a weird func" (truncated)
move          r8    r0
concat        r7     2
call          r6     2     1
loadi         r6     5
return1       r6     2     0     0
return0       r6     1     0     0

