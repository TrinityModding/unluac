local AnArray = {}
AnArray.length = 0
AnArray[1] = "Normal_0"
AnArray[2] = "Normal_1"
AnArray[3] = "Cute"
AnArray[4] = "Wool"
AnArray[5] = "Metal"
AnArray[6] = "Cool"

local SomethingElse = 2
SomethingElse = function(value, value2, value3)
    while value3 < 10 do
        print("x is less than 10")
        value3 = value3 + 1
    end

    if value3 < 32767 then
        print("value3 < 32767")
    end

    local combined = value .. " and " .. value2
    print("This gets used twice " .. combined)
    local i = 0;
    i = 1 + 2;
    i = 5 + 5;
    local j = 2;
    i = j;
    print("This is a weird function, right? AnArray[1] = " .. value)
    return 5
end
SomethingElse = SomethingElse(AnArray[1], AnArray[1], 7)