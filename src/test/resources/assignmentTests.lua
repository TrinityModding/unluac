local AnArray = {}
AnArray.length = 0
AnArray[1] = "Normal_0"
AnArray[2] = "Normal_1"
AnArray[3] = "Cute"
AnArray[4] = "Wool"
AnArray[5] = "Metal"
AnArray[6] = "Cool"

local SomethingElse = 2
SomethingElse = function(value)
    print("This gets used twice" .. value)
    local i = 0;
    print("This is a weird function, right? AnArray[1] = " .. value)
    return 5
end
SomethingElse = SomethingElse(AnArray[1])