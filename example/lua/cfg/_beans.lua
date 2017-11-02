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


Beans.range = {}
function Beans.range:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.min = os:ReadInt32() -- 最小
    o.max = os:ReadInt32() -- 最大
    return o
end


Beans.task = {}
Beans.task.completecondition = {}
function Beans.task.completecondition:_create(os)
    local s = os:ReadString()
    if s == 'KillMonster' then
        return Beans.task.completecondition.killmonster:_create(os)
    elseif s == 'TalkNpc' then
        return Beans.task.completecondition.talknpc:_create(os)
    elseif s == 'CollectItem' then
        return Beans.task.completecondition.collectitem:_create(os)
    end
end
Beans.task.completecondition.killmonster = {}
function Beans.task.completecondition.killmonster:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.monsterid = os:ReadInt32()
    o.RefMonsterid = nil
    o.count = os:ReadInt32()
    return o
end


function Beans.task.completecondition.killmonster:type()
    return 'KillMonster'
end

Beans.task.completecondition.talknpc = {}
function Beans.task.completecondition.talknpc:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.npcid = os:ReadInt32()
    return o
end


function Beans.task.completecondition.talknpc:type()
    return 'TalkNpc'
end

Beans.task.completecondition.collectitem = {}
function Beans.task.completecondition.collectitem:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.itemid = os:ReadInt32()
    o.count = os:ReadInt32()
    return o
end


function Beans.task.completecondition.collectitem:type()
    return 'CollectItem'
end

return Beans
