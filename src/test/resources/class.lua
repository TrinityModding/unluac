TestClass = {}

function TestClass:new (o)
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

function TestClass:deposit (money)
    self.balance = self.balance + money
end

a = TestClass:new{balance = 0}
a:deposit(100.0)