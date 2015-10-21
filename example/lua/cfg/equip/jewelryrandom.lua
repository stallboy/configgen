local Beans = require("cfg._beans")

local jewelryrandom = {}
jewelryrandom.all = {}

function jewelryrandom._create(os)
    local o = {}
    o.lvlRank = Beans.levelrank._create(os) -- 等级
    o.attackRange = Beans.range._create(os) -- 最小攻击力
    o.otherRange = {} -- 最小防御力
    for _ = 1, os:ReadSize() do
        table.insert(o.otherRange, Beans.range._create(os))
    end
    return o
end

function jewelryrandom.get(lvlRank)
    return jewelryrandom.all[lvlRank]
end

function jewelryrandom._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelryrandom._create(os)
        jewelryrandom.all[v.lvlRank] = v
    end
end

return jewelryrandom
