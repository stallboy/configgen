local Beans = require("cfg._beans")

local monster = {}

function monster:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.id = os:ReadInt32() -- id
    o.posList = {}
    for _ = 1, os:ReadSize() do
        table.insert(o.posList, Beans.position:_create(os))
    end
    return o
end


monster.all = {}
function monster.get(id)
    return monster.all[id]
end

function monster._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = monster:_create(os)
        monster.all[v.id] = v
    end
end

return monster
