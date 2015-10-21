local Beans = require("cfg._beans")

local monster = {}
monster.all = {}

function monster._create(os)
    local o = {}
    o.id = os:ReadInt32() -- id
    o.pos = Beans.position._create(os)
    return o
end

function monster.get(id)
    return monster.all[id]
end

function monster._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = monster._create(os)
        monster.all[v.id] = v
    end
end

return monster
