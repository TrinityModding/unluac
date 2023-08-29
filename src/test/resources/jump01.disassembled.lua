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
.maxstacksize	4

.upvalue	"_ENV"	0	true

.constant	k0	nil

varargprep     0
loadi         r0     0
eqk           r0    k0     1     0 ; k0 = nil
jmp          l14
closure       r1    f0
move          r2    r1
gti           r0    10     0     0
jmp          l12
move          r3    r2
call          r3     1     1
jmp          l14
.label	l12
move          r3    r2
call          r3     1     1
.label	l14
return        r1     1     1     0

.function	main/f0

.source	null
.linedefined	3
.lastlinedefined	5
.numparams	0
.is_vararg	0
.maxstacksize	2

.upvalue	null	0	false

.constant	k0	"print"
.constant	k1	L"I long for the great release of inlined code"

gettabup      r0    u0    k0 ; k0 = "print"
loadk         r1    k1 ; k1 = L"I long for the great" (truncated)
call          r0     2     1
return0       r0     1     0     0

