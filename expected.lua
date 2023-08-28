.version	5.4

.format	0
.instruction_size	4
.integer_format	8
.float_format	8
.endianness	LITTLE

.function	main

.source	"@D:\\NewProjects\\Pokemon\\TrinityModding\\unluac\\build\\test\\52_goto02.lua"
.linedefined	0
.lastlinedefined	0
.numparams	0
.is_vararg	1
.maxstacksize	11

.local	"(for state)"	4	22
.local	"(for state)"	4	22
.local	"(for state)"	4	22
.local	"x"	5	21
.local	"(for state)"	8	21
.local	"(for state)"	8	21
.local	"(for state)"	8	21
.local	"y"	9	20

.upvalue	"_ENV"	0	true

.constant	k0	"print"
.constant	k1	"f"

.line	1	varargprep     0
.line	0	loadi         r0     1
.line	0	loadi         r1    10
.line	0	loadi         r2     1
.line	0	forprep       r0   l22
.label	l6
.line	1	loadi         r4     1
.line	0	loadi         r5    10
.line	0	loadi         r6     1
.line	0	forprep       r4   l21
.label	l10
.line	1	gettabup      r8    u0    k0 ; k0 = "print"
.line	0	move          r9    r3
.line	0	move         r10    r7
.line	0	call          r8     3     1
.line	1	gettabup      r8    u0    k1 ; k1 = "f"
.line	0	move          r9    r3
.line	0	move         r10    r7
.line	0	call          r8     3     2
.line	0	test          r8     0
.line	0	jmp          l21
.line	1	jmp          l23
.label	l21
.line	253	forloop       r4   l10
.label	l22
.line	255	forloop       r0    l6
.label	l23
.line	8	return        r0     1     1     0

