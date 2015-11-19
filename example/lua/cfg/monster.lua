local Beans = require("cfg._beans")

local monster = {}
monster.all = {}

function monster:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.id = os:ReadInt32() -- id
    o.pos = Beans.position:_create(os)
    return o
end

function monster:_assign(other)
    self.id = other.id
    self.pos:_assign(other.pos)
end

function monster.get(id)
    return monster.all[id]
end

function monster._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = monster:_create(os)
        monster.all[v.id] = v
    end
end

function monster._reload(os, errors)
    local old = monster.all
    monster.all = {}
    monster._initialize(os, errors)
    for k, v in pairs(monster.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return monster
