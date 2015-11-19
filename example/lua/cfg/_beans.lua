local Beans = {}
Beans.levelrank = {}
function Beans.levelrank:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.level = os:ReadInt32() -- 等级
    o.rank = os:ReadInt32() -- 品质
    o.RefRank = nil
    return o
end

function Beans.levelrank:_assign(other)
    self.level = other.level
    self.rank = other.rank
end

Beans.position = {}
function Beans.position:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.x = os:ReadInt32()
    o.y = os:ReadInt32()
    o.z = os:ReadInt32()
    return o
end

function Beans.position:_assign(other)
    self.x = other.x
    self.y = other.y
    self.z = other.z
end

Beans.range = {}
function Beans.range:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.min = os:ReadInt32() -- 最小
    o.max = os:ReadInt32() -- 最大
    return o
end

function Beans.range:_assign(other)
    self.min = other.min
    self.max = other.max
end

return Beans
