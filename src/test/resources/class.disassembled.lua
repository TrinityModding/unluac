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
.maxstacksize	3

.upvalue	"_ENV"	0	true

.constant	k0	"TestClass"
.constant	k1	"new"
.constant	k2	"deposit"
.constant	k3	"a"
.constant	k4	"balance"
.constant	k5	0

varargprep     0
newtable      r0     0     0     0
extraarg       0
settabup      u0    k0    r0 ; k0 = "TestClass"
gettabup      r0    u0    k0 ; k0 = "TestClass"
closure       r1    f0
setfield      r0    k1    r1 ; k1 = "new"
gettabup      r0    u0    k0 ; k0 = "TestClass"
closure       r1    f1
setfield      r0    k2    r1 ; k2 = "deposit"
gettabup      r0    u0    k0 ; k0 = "TestClass"
self          r0    r0    k1 ; k1 = "new"
newtable      r2     1     0     0
extraarg       0
setfield      r2    k4    k5 ; k5 = 0
call          r0     3     2
settabup      u0    k3    r0 ; k3 = "a"
gettabup      r0    u0    k3 ; k3 = "a"
self          r0    r0    k2 ; k2 = "deposit"
loadf         r2   100
call          r0     3     1
return        r0     1     1     0

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

