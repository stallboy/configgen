local loot = {}

function loot:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.lootid = os:ReadInt32() -- 序号
    o.ename = os:ReadString()
    o.name = os:ReadString() -- 名字
    o.chanceList = {} -- 掉落0件物品的概率
    for _ = 1, os:ReadSize() do
        table.insert(o.chanceList, os:ReadInt32())
    end
    o.ListRefLootid = {}
    return o
end


loot.all = {}
function loot.get(lootid)
    return loot.all[lootid]
end

function loot._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = loot:_create(os)
        loot.all[v.lootid] = v
    end
end

return loot
