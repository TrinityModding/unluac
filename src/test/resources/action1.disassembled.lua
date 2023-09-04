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
.maxstacksize	8

.upvalue	"_ENV"	0	true

.constant	k0	" = apple"
.constant	k1	" + "

varargprep     0
loadi         r0     6
move          r1    r0
loadi         r2     5
loadk         r3    k0 ; k0 = " = apple"
move          r4    r1
loadk         r5    k1 ; k1 = " + "
move          r6    r2
move          r7    r3
concat        r4     4
return        r5     1     1     0

