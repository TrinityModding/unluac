## How to fix method output:
1. Find method call
2. search backwards from the call until you hit the statement assigning the function name
3. Inline Everything inside of this
4. Have a perfectly good function

#### IF THE FUNCTION IS FROM AN ASSIGNMENT:
it follows this pattern:
```LUA
A = foo()
B = A
```
but the result should be
```LUA
B = foo()
```
as A is only temporary. However, even though its temporary it may be used
later on in other locations, so it cannot be destroyed.


## How to fix variable reassigning: (Must happen after the above step, or it will break both)
1. Keep track every time a local is assigned. if that local is already assigned, put that
local in another list and map it to a new local name you generate
2. every line you pass over you must do a deep search for the locals that need replacing
and replace them. Missing a single one can break the logic of the program.

## Notes:
Step 3 from 2nd method can most likely be reused to remap from hashed to named names