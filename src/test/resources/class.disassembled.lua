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

.constant	k0	"new"
.constant	k1	"deposit"
.constant	k2	"balance"
.constant	k3	0

varargprep     0
newtable      r0     0     0     0
extraarg       0
closure       r1    f0
setfield      r0    k0    r1 ; k0 = "new"
closure       r1    f1
setfield      r0    k1    r1 ; k1 = "deposit"
self          r1    r0    k0 ; k0 = "new"
newtable      r3     1     0     0
extraarg       0
setfield      r3    k2    k3 ; k3 = 0
call          r1     3     2
self          r2    r1    k1 ; k1 = "deposit"
loadf         r4   100
call          r2     3     1
return        r2     1     1     0

.function	main/f0

.source	null
.linedefined	3
.lastlinedefined	8
.numparams	2
.is_vararg	0
.maxstacksize	5

.upvalue	null	0	false

.constant	k0	"setmetatable"
.constant	k1	"__index"

test          r1     1
jmp           l6
newtable      r2     0     0     0
extraarg       0
move          r1    r2
.label	l6
gettabup      r2    u0    k0 ; k0 = "setmetatable"
move          r3    r1
move          r4    r0
call          r2     3     1
setfield      r0    k1    r0 ; k1 = "__index"
return1       r1     2     0     0
return0       r2     1     0     0

.function	main/f1

.source	null
.linedefined	10
.lastlinedefined	12
.numparams	2
.is_vararg	0
.maxstacksize	3

.constant	k0	"balance"

getfield      r2    r0    k0 ; k0 = "balance"
add           r2    r2    r1
mmbin         r2    r1     6
setfield      r0    k0    r2 ; k0 = "balance"
return0       r2     1     0     0

