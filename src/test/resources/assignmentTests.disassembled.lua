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
.maxstacksize	2

.upvalue	"_ENV"	0	true

.constant	k0	"AnArray"
.constant	k1	"length"
.constant	k2	0
.constant	k3	"Normal_0"
.constant	k4	"Normal_1"
.constant	k5	"Cute"
.constant	k6	"Wool"
.constant	k7	"Metal"
.constant	k8	"Cool"

varargprep     0
newtable      r0     0     0     0
extraarg       0
settabup      u0    k0    r0 ; k0 = "AnArray"
gettabup      r0    u0    k0 ; k0 = "AnArray"
setfield      r0    k1    k2 ; k2 = 0
gettabup      r0    u0    k0 ; k0 = "AnArray"
seti          r0     1    k3 ; k3 = "Normal_0"
gettabup      r0    u0    k0 ; k0 = "AnArray"
seti          r0     2    k4 ; k4 = "Normal_1"
gettabup      r0    u0    k0 ; k0 = "AnArray"
seti          r0     3    k5 ; k5 = "Cute"
gettabup      r0    u0    k0 ; k0 = "AnArray"
seti          r0     4    k6 ; k6 = "Wool"
gettabup      r0    u0    k0 ; k0 = "AnArray"
seti          r0     5    k7 ; k7 = "Metal"
gettabup      r0    u0    k0 ; k0 = "AnArray"
seti          r0     6    k8 ; k8 = "Cool"
return        r0     1     1     0

