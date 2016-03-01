local Beans = require("cfg._beans")

local jewelryrandom = {}

function jewelryrandom:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.lvlRank = Beans.levelrank:_create(os) -- 等级
    o.attackRange = Beans.range:_create(os) -- 最小攻击力
    o.otherRange = {} -- 最小防御力
    for _ = 1, os:ReadSize() do
        table.insert(o.otherRange, Beans.range:_create(os))
    end
    return o
end

function jewelryrandom:_assign(other)
    self.lvlRank:_assign(other.lvlRank)
    self.attackRange:_assign(other.attackRange)
    for k, v in pairs(other.otherRange) do
        self.otherRange[k] = v
    end
end

jewelryrandom.all = {}
function jewelryrandom.get(lvlRank)
    return jewelryrandom.all[lvlRank]
end

function jewelryrandom._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelryrandom:_create(os)
        jewelryrandom.all[v.lvlRank] = v
    end
end

function jewelryrandom._reload(os, errors)
    local old = jewelryrandom.all
    jewelryrandom.all = {}
    jewelryrandom._initialize(os, errors)
    for k, v in pairs(jewelryrandom.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return jewelryrandom
