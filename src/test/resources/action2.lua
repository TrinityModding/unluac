local function e(...)
    local printResult

    for i,v in ipairs(arg) do
        printResult = printResult .. tostring(v) .. "\t"
    end

    print(printResult)
end

local six = 6
local a = six
local b = 5
local c = " = apple"

local result = e(a, " + ", b, c)
