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

function lootitem:_assign(other)
    self.lootid = other.lootid
    self.itemid = other.itemid
    self.chance = other.chance
    self.countmin = other.countmin
    self.countmax = other.countmax
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

function lootitem._reload(os, errors)
    local old = lootitem.all
    lootitem.all = {}
    lootitem._initialize(os, errors)
    for k, v in pairs(lootitem.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return lootitem
