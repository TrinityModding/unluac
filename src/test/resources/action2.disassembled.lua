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
.maxstacksize	10

.upvalue	"_ENV"	0	true

.constant	k0	" = apple"
.constant	k1	" + "

varargprep     0
closure       r0    f0
loadi         r1     6
move          r2    r1
loadi         r3     5
loadk         r4    k0 ; k0 = " = apple"
move          r5    r0
move          r6    r2
loadk         r7    k1 ; k1 = " + "
move          r8    r3
move          r9    r4
call          r5     5     2
return        r6     1     1     0

.function	main/f0

.source	null
.linedefined	1
.lastlinedefined	9
.numparams	0
.is_vararg	1
.maxstacksize	10

.upvalue	null	0	false

.constant	k0	"ipairs"
.constant	k1	"arg"
.constant	k2	"tostring"
.constant	k3	"\t"
.constant	k4	"print"

varargprep     0
loadnil       r0     0
gettabup      r1    u0    k0 ; k0 = "ipairs"
gettabup      r2    u0    k1 ; k1 = "arg"
call          r1     2     5
tforprep      r1   l14
.label	l7
move          r7    r0
gettabup      r8    u0    k2 ; k2 = "tostring"
move          r9    r6
call          r8     2     2
loadk         r9    k3 ; k3 = "\t"
concat        r7     3
move          r0    r7
.label	l14
tforcall      r1     2
tforloop      r1    l7
close         r1
gettabup      r1    u0    k4 ; k4 = "print"
move          r2    r0
call          r1     2     1
return        r1     1     1     1

