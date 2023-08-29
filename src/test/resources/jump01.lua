local localVar = 0
if nil ~= localVar then
    local function e()
        print("I long for the great release of inlined code")
    end

    local someOtherLocal = e;

    if localVar > 10 then
        someOtherLocal()
    else
        someOtherLocal()
    end
end
