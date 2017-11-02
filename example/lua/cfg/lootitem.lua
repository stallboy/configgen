local lootitem = {}

function lootitem:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.lootid = os:ReadInt32() -- 掉落id
    o.itemid = os:ReadInt32() -- 掉落物品
    o.chance = os:ReadInt32() -- 掉落概率
    o.countmin = os:ReadInt32() -- 数量下限
    o.countmax = os:ReadInt32() -- 数量上限
    return o
end


lootitem.all = {}
function lootitem.get(lootid, itemid)
    return lootitem.all[lootid ..",".. itemid]
end

function lootitem._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = lootitem:_create(os)
        lootitem.all[v.lootid ..",".. v.itemid] = v
    end
end

return lootitem
